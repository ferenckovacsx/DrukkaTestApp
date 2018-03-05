package com.example.drukkatestapp.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.drukkatestapp.DocumentsListAdapter;
import com.example.drukkatestapp.R;
import com.example.drukkatestapp.pojo.DeleteFilePOJO;
import com.example.drukkatestapp.pojo.FilePOJO;
import com.example.drukkatestapp.retrofit.APIClient;
import com.example.drukkatestapp.retrofit.APIInterface;

import java.io.File;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    final String TAG = "DOCUMENTSACTIVITY";

    int selectedItemPosition;
    int fileCount;
    int counter = 1;
    String selectedItemUUID;

    private RecyclerView recyclerView;
    private DocumentsListAdapter listAdapter;
    private RecyclerView.LayoutManager layoutManager;

    ImageView addNewDocumentButton, deleteDocumentButton, logoutButton, doneButton;
    TextView emptyListTextView, documentsHeaderTextView;

    ArrayList<FilePOJO> listOfFiles = new ArrayList<>();

    APIInterface apiInterface;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //UI elements
        recyclerView = findViewById(R.id.documents_list);
        addNewDocumentButton = findViewById(R.id.add_new_document);
        deleteDocumentButton = findViewById(R.id.delete_document);
        logoutButton = findViewById(R.id.logout);
        doneButton = findViewById(R.id.done);
        emptyListTextView = findViewById(R.id.empty_list_textview);
        documentsHeaderTextView = findViewById(R.id.header_name);

        if (listOfFiles.size() == 0) {
            documentsHeaderTextView.setVisibility(View.GONE);
            emptyListTextView.setVisibility(View.VISIBLE);
        }

        progressDialog = new ProgressDialog(this);

        //retrofit
        APIClient apiClient = new APIClient(this);
        apiInterface = apiClient.getClient().create(APIInterface.class);

        Log.i(TAG, "number of files: " + listOfFiles.size());

        getDocuments();

        addNewDocumentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent selectFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                selectFileIntent.setType("*/*");
                selectFileIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(selectFileIntent, 1);
            }
        });

        deleteDocumentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deleteDocument(selectedItemUUID);

            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                logout();

            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                dialogBuilder.setTitle("Finish process.");
                dialogBuilder.setMessage("Are you sure? \nYour account will be inactivated.");
                dialogBuilder.setCancelable(true);

                dialogBuilder.setPositiveButton(
                        "Yes, I'm done",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                close();
                            }
                        });

                dialogBuilder.setNegativeButton(
                        "Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = dialogBuilder.create();
                alert11.show();

            }
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {

            Log.i(TAG, "COUNTER: " + counter);

            Uri uri;

            if (data.getClipData() == null) {
                uri = data.getData();
                addDocuments(uri);
                fileCount = 1;

            } else {
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    uri = data.getClipData().getItemAt(i).getUri();
                    addDocuments(uri);
                    fileCount = data.getClipData().getItemCount();
                }
            }
        }
    }

    void getDocuments() {

        SharedPreferences cookiePref = getApplicationContext().getSharedPreferences("cookiePref", MODE_PRIVATE);
        String cookieValue = cookiePref.getString("cookieValue", "");
        Log.i(TAG, "cookieValue: " + cookieValue);

        Call<ArrayList<FilePOJO>> call = apiInterface.list_documents(cookieValue);

        call.enqueue(new Callback<ArrayList<FilePOJO>>() {

            @Override
            public void onResponse(Call<ArrayList<FilePOJO>> call, retrofit2.Response<ArrayList<FilePOJO>> response) {

                int responseCode = response.code();
                switch (responseCode) {
                    case 200:
                        Log.i(TAG, "Fetching data was successful. Number of files: " + response.body().size());

                        listOfFiles = response.body();
                        if (listOfFiles.size() > 0) {
                            emptyListTextView.setVisibility(View.GONE);
                            documentsHeaderTextView.setVisibility(View.VISIBLE);
                        } else {
                            emptyListTextView.setVisibility(View.VISIBLE);
                            documentsHeaderTextView.setVisibility(View.GONE);
                        }

                        //recyclerview with custom adapter
                        recyclerView.setHasFixedSize(true);
                        layoutManager = new LinearLayoutManager(MainActivity.this);
                        recyclerView.setLayoutManager(layoutManager);

                        //add divider between list items
                        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), 1);
                        recyclerView.addItemDecoration(dividerItemDecoration);

                        listAdapter = new DocumentsListAdapter(listOfFiles);
                        recyclerView.setAdapter(listAdapter);

                        listAdapter.setOnItemSelectedListener(new DocumentsListAdapter.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(int position, String uuid) {
                                deleteDocumentButton.setVisibility(View.VISIBLE);
                                selectedItemPosition = position;
                                selectedItemUUID = uuid;
                                Log.i(TAG, "Item is selected: " + uuid);
                            }
                        });

                        break;

                    default:
                        Log.i(TAG, "Unkwonwn error");
                }
            }

            @Override
            public void onFailure(Call<ArrayList<FilePOJO>> call, Throwable t) {
                Log.e(TAG, "Get files error" + t.toString());
            }
        });
    }

    private void deleteDocument(String uuid) {

        progressDialog.setMessage("Deleting document...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        SharedPreferences cookiePref = getApplicationContext().getSharedPreferences("cookiePref", MODE_PRIVATE);
        String cookieValue = cookiePref.getString("cookieValue", "");
        Log.i(TAG, "cookieValue: " + cookieValue);

        Call<ResponseBody> call = apiInterface.delete_document(cookieValue, new DeleteFilePOJO(uuid));

        call.enqueue(new Callback<ResponseBody>() {


            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {

                int responseCode = response.code();
                switch (responseCode) {
                    case 200:

                        progressDialog.cancel();
                        deleteDocumentButton.setVisibility(View.GONE);
                        getDocuments();

                        Log.i(TAG, "Deletion successful");
                        break;

                    case 401:
                        Log.i(TAG, "Not logged in");
                        break;

                    case 400:
                        Log.i(TAG, "Bad request");
                        break;

                    default:
                        Log.i(TAG, "Unkown error");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.cancel();
                Log.e(TAG, "delete document error" + t.toString());
            }
        });
    }

    private void addDocuments(Uri fileuri) {

        //only show progressdialog once
        if (counter == 1) {
            progressDialog.setMessage("Uploading. Please wait.");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        //not working :(
//        List<MultipartBody.Part> parts = new ArrayList<>();
//
//        for (int i = 0; i < uris.size() ; i++) {
//
//            File file = new File(getPath(MainActivity.this, uris.get(i)));
//
//            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
//            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
//
//            parts.add(body);
//        }

        String filePathFromUri = getPath(MainActivity.this, fileuri);

        File file = new File(filePathFromUri);

//        RequestBody requestFile = RequestBody.create(MediaType.parse("application/pdf"), file);
        RequestBody requestFile = RequestBody.create(MediaType.parse(getMimeType(filePathFromUri)), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

//        retrieve cookie from shared preferences
        SharedPreferences cookiePref = getApplicationContext().getSharedPreferences("cookiePref", MODE_PRIVATE);
        String cookieValue = cookiePref.getString("cookieValue", "");

        Call<ResponseBody> call = apiInterface.add_documents(cookieValue, body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                int responseCode = response.code();
                switch (responseCode) {
                    case 201:

                        if (fileCount == counter) {
                            progressDialog.cancel();
                            counter = 0;
                            Toast.makeText(MainActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                        }
                        counter += 1;


                        //refresh list with new data from server
                        getDocuments();

                        Log.i(TAG, "201: Document upload successful");
                        break;

                    case 401:
                        Log.i(TAG, "401: Not logged in");
                        break;

                    case 400:
                        Log.i(TAG, "400: Bad request");
                        break;

                    default:
                        Log.i(TAG, "Unkwnown error");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
                progressDialog.cancel();
                Toast.makeText(MainActivity.this, "Timout error. File was not uploaded.", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void logout() {

        SharedPreferences cookiePref = getApplicationContext().getSharedPreferences("cookiePref", MODE_PRIVATE);
        String cookieValue = cookiePref.getString("cookieValue", "");
        Log.i(TAG, "cookieValue: " + cookieValue);

        Call<ResponseBody> call = apiInterface.logout(cookieValue);

        call.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {

                int responseCode = response.code();
                switch (responseCode) {
                    case 200:

                        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(loginIntent);

                        MainActivity.this.finish();

                        Log.i(TAG, "200: Logout successful ");
                        break;

                    case 401:
                        Log.i(TAG, "401: Not logged in");
                        break;

                    case 400:
                        Log.i(TAG, "400: Bad request");
                        break;

                    default:
                        Log.i(TAG, "Unkwnown error");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "logout error" + t.toString());
            }
        });
    }

    private void close() {

        SharedPreferences cookiePref = getApplicationContext().getSharedPreferences("cookiePref", MODE_PRIVATE);
        String cookieValue = cookiePref.getString("cookieValue", "");
        Log.i(TAG, "cookieValue: " + cookieValue);

        Call<ResponseBody> call = apiInterface.close(cookieValue);

        call.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {

                int responseCode = response.code();
                switch (responseCode) {
                    case 200:

                        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(loginIntent);

                        Toast.makeText(MainActivity.this, "Process finished. Good luck!", Toast.LENGTH_SHORT).show();

                        MainActivity.this.finish();

                        Log.i(TAG, "200: Account closed ");
                        break;

                    case 401:
                        Log.i(TAG, "401: Not logged in");
                        break;

                    case 400:
                        Log.i(TAG, "400: Bad request");
                        break;

                    default:
                        Log.i(TAG, "Unkwnown error");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "logout error" + t.toString());
            }
        });
    }


    public static String getPath(final Context context, final Uri uri) {

        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return "";
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }

        Log.i("getMimeType()", "MIMETYPE: " + type);
        return type;
    }

}
