package com.example.drukkatestapp.ui;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.drukkatestapp.DocumentsListAdapter;
import com.example.drukkatestapp.pojo.FilePOJO;
import com.example.drukkatestapp.R;
import com.example.drukkatestapp.retrofit.APIClient;
import com.example.drukkatestapp.retrofit.APIInterface;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.os.Environment.DIRECTORY_PICTURES;

public class MainActivity extends AppCompatActivity {

    String TAG = "DOCUMENTSACTIVITY";
    int selectedItemPosition;
    String selectedItemUUID;

    private RecyclerView recyclerView;
    private DocumentsListAdapter listAdapter;
    private RecyclerView.LayoutManager layoutManager;

    ImageView addNewDocumentBtn;
    ImageView deleteDocumentBtn;
    ImageView logoutBtn;

    ArrayList<FilePOJO> listOfFiles;

    APIInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //UI elements
        recyclerView = findViewById(R.id.documents_list);
        addNewDocumentBtn = findViewById(R.id.add_new_document);
        deleteDocumentBtn = findViewById(R.id.delete_document);
        logoutBtn = findViewById(R.id.logout);

        //retrofit
        APIClient apiClient = new APIClient(this);
        apiInterface = apiClient.getClient().create(APIInterface.class);

        listOfFiles = getDocuments();

        Log.i(TAG, "number of files: " + listOfFiles.size());

        //recyclerview with custom adapter
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //add divider between list items
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), 1);
        recyclerView.addItemDecoration(dividerItemDecoration);

        listAdapter = new DocumentsListAdapter(listOfFiles);
        recyclerView.setAdapter(listAdapter);


        addNewDocumentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent selectFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                selectFileIntent.setType("*/*");
                selectFileIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(selectFileIntent, 1);
            }
        });

        deleteDocumentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deleteDocument(selectedItemUUID);

            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                logout();

            }
        });


        listAdapter.setOnItemSelectedListener(new DocumentsListAdapter.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int position, String uuid) {
                deleteDocumentBtn.setVisibility(View.VISIBLE);
                selectedItemPosition = position;
                selectedItemUUID = uuid;
                Log.i(TAG, "item is selected: " + uuid);
            }
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {


            Log.i(TAG, "filePath: " + getPath(MainActivity.this, data.getData()));

            File file = new File(getPath(MainActivity.this, data.getData()));
            long fileSize = file.length()/1024;
            Log.i(TAG, "selected filesize: " + fileSize);

            uploadDocument(data.getData());

        }
    }


    ArrayList<FilePOJO> getDocuments() {

        final ArrayList<FilePOJO> documents = new ArrayList<>();

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

                        //add retrieved data to arraylist
                        documents.addAll(response.body());

                        //notify adapter about newly retrieved items
                        listAdapter.notifyDataSetChanged();

                        Log.i(TAG, "Fetching data was successful. Number of files: " + response.body().size());
                        break;

                    default:
                        Log.i(TAG, "Unauthorized");
                }
            }

            @Override
            public void onFailure(Call<ArrayList<FilePOJO>> call, Throwable t) {


            }
        });

        return documents;
    }

    private void deleteDocument(String uuid) {

        SharedPreferences cookiePref = getApplicationContext().getSharedPreferences("cookiePref", MODE_PRIVATE);
        String cookieValue = cookiePref.getString("cookieValue", "");
        Log.i(TAG, "cookieValue: " + cookieValue);

        Call<ResponseBody> call = apiInterface.delete_document(cookieValue, uuid);

        call.enqueue(new Callback<ResponseBody>() {


            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {

                int responseCode = response.code();
                switch (responseCode) {
                    case 200:

                        listOfFiles.remove(selectedItemPosition);
                        listAdapter.notifyItemRemoved(selectedItemPosition);
                        listAdapter.notifyItemRangeChanged(selectedItemPosition, listOfFiles.size());

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


            }
        });
    }

    private void uploadDocument(Uri fileUri) {

        String testFilePath = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES) + "/" + "1519950277941.jpg";

        // create RequestBody instance from file
        RequestBody requestFile = RequestBody.create(MediaType.parse(getContentResolver().getType(fileUri)), testFilePath);

        Log.i(TAG, "filePath: " + getPath(MainActivity.this, fileUri));
//        RequestBody requestFile = RequestBody.create(MediaType.parse(getContentResolver().getType(fileUri)), getPath(MainActivity.this, fileUri));

        MultipartBody.Part body = MultipartBody.Part.createFormData("doc", getFileNameFromURI(fileUri), requestFile);

        //retrieve cookie from shared preferences
        SharedPreferences cookiePref = getApplicationContext().getSharedPreferences("cookiePref", MODE_PRIVATE);
        String cookieValue = cookiePref.getString("cookieValue", "");


        Call<ResponseBody> call = apiInterface.add_documents(cookieValue, body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.v("Upload", "success: " + response.code());

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getDocuments();
                    }
                }, 2000);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
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

                        Log.i(TAG, "Logout successful ");
                        break;

                    case 401:
                        Log.i(TAG, "Not logged in");
                        break;

                    case 400:
                        Log.i(TAG, "Bad request");
                        break;

                    default:
                        Log.i(TAG, "Unauthorized");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {


            }
        });
    }

    public String getPathFromURI(Uri uri) {

        String fileName = getFileNameFromURI(uri);
        File tempFile;

        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);

            try {
                byte[] buffer = new byte[inputStream.available()];
                inputStream.read(buffer);

                tempFile = new File(MainActivity.this.getFilesDir(), fileName);
                OutputStream outStream = new FileOutputStream(tempFile);
                outStream.write(buffer);
                outStream.close();

                Log.i(TAG, "temp fileName: " + tempFile.getName());
                Log.i(TAG, "temp filePath: " + tempFile.getPath());
                Log.i(TAG, "temp fileAbsPath: " + tempFile.getAbsolutePath());

                return tempFile.getPath();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getFileNameFromURI(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }


    String savefile(Uri sourceuri) {

        String sourceFilename = sourceuri.getPath();
        String destinationFilename = MainActivity.this.getFilesDir() + "/" + getFileNameFromURI(sourceuri);

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        try {
            bis = new BufferedInputStream(new FileInputStream(sourceFilename));
            bos = new BufferedOutputStream(new FileOutputStream(destinationFilename, false));
            byte[] buf = new byte[1024];
            bis.read(buf);
            do {
                bos.write(buf);
            } while (bis.read(buf) != -1);

            return destinationFilename;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) bis.close();
                if (bos != null) bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return destinationFilename;
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

                // TODO handle non-primary volumes
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
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

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
}
