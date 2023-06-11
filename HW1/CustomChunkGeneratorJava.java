package cz.cuni.gamedev.nail123.mcworldgeneration;

import cz.cuni.gamedev.nail123.mcworldgeneration.chunking.IChunk;
import org.bukkit.Material;
import org.bukkit.util.noise.OctaveGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.lang.Math;

public class CustomChunkGeneratorJava implements IChunkGenerator {
    public long seed;
    OctaveGenerator generator;

    OctaveGenerator temperatureGenerator;
    OctaveGenerator precipitationGenerator;

    final int areaSize = 16;

    public CustomChunkGeneratorJava(long seed) {
        this.seed = seed;
        this.generator = new SimplexOctaveGenerator(new Random(this.seed), 8);
        this.generator.setScale(0.00001);

        this.temperatureGenerator = new SimplexOctaveGenerator(new Random(this.seed), 1);
        this.temperatureGenerator.setScale(0.004);
        this.precipitationGenerator = new SimplexOctaveGenerator(new Random(this.seed + 1), 1);
        this.precipitationGenerator.setScale(0.004);
    }
    public CustomChunkGeneratorJava() {
        this((new Random()).nextLong());
    }

    private enum Biome {sea, mountain, grass, desert};

    private Biome getBiome( int X, int Z) {
        double temp = temperatureGenerator.noise(X, Z, 1, 2, true);
        double prec = precipitationGenerator.noise(X, Z, 1, 2, true);

        double temperature = (temp+1)*35 - 15;
        double precipitation = (prec+1)*35 - 10;
        
        // Whittaker diagram
        if (precipitation <= -0.125*temperature*temperature + 30)
            return Biome.mountain;
        else if (precipitation <= Math.sqrt((temperature-20)*40))
            return Biome.desert;
        else if (precipitation >= 35 || precipitation >= -1.5*temperature + 75)
            return Biome.sea;
        else
            return Biome.grass;
    }

    private double getBiomeHeight(Biome biome, double noise) {

        switch (biome)
        {
            case sea:
                return (noise+1)*10 + 50;
            case desert:
                return (noise+1)*20 + 40;
            case mountain:
                return (noise+1)*45 + 60;
            default:
                return  (noise+1)*35 + 45;
        }
    }

    private int filter(int X, int Z) {
        int size = 2*areaSize + 1;
        double amount = 0;
        double sum = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int x = X - areaSize + i;
                int z = Z - areaSize + j;

                Biome biome = getBiome(x, z);
                double noise = generator.noise(x, z, 3.0, 1.2, true);
                
                double modifier = (((i+j)%(areaSize+1)+1)/5);
                sum += modifier*getBiomeHeight(biome, noise);
                amount += modifier;
            }
        }
        return (int) (sum/amount);
    }

    public void generateChunk(int chunkX, int chunkZ, @NotNull IChunk chunk) {
        for (int X = 0; X <= 15; ++X) {
            for (int Z = 0; Z <= 15; ++Z) {
                // X and Z are the geographic coordinates in Minecraft
                int currentHeight = filter(chunkX*16 + X, chunkZ*16 + Z);

                // Set top layer of material based on height
                for (int i = currentHeight + 1; i <= 62; ++i) {
                    chunk.set(X, i, Z, Material.WATER);
                }
                if (currentHeight <= 70) {
                    chunk.set(X, currentHeight, Z, Material.SAND);
                } else if (currentHeight <= 85) {
                    chunk.set(X, currentHeight, Z, Material.GRASS_BLOCK);
                } else {
                    chunk.set(X, currentHeight, Z, Material.SNOW);
                }

                // Place dirt one block underneath

                chunk.set(X, currentHeight - 1, Z, Material.DIRT);

                // Place stone all the way to the bottom, except for the last layer
                for (int i = currentHeight - 2; i >= 1; --i) {
                    chunk.set(X, i, Z, Material.STONE);
                }

                // ... which is bedrock
                chunk.set(X, 0, Z, Material.BEDROCK);
            }
        }
    }
}
