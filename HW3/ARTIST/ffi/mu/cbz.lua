typedef struct cbz_document cbz_document;
typedef struct cbz_page     cbz_page;
cbz_document*  cbz_open_document(             fz_context*,   char *filename);
cbz_document*  cbz_open_document_with_stream( fz_stream*     );
void           cbz_close_document(            cbz_document*  );
int            cbz_count_pages(               cbz_document*  );
cbz_page*      cbz_load_page(                 cbz_document*, int number );
fz_rect        cbz_bound_page(                cbz_document*, cbz_page*  );
void           cbz_free_page(                 cbz_document*, cbz_page*  );
void           cbz_run_page(                  cbz_document*, cbz_page*, fz_device*, fz_matrix ctm, fz_cookie* );
