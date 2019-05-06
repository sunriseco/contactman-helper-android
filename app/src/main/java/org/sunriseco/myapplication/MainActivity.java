package org.sunriseco.myapplication;

import android.Manifest;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.thanosfisherman.mayi.Mayi;
import com.thanosfisherman.mayi.PermissionBean;
import com.thanosfisherman.mayi.PermissionToken;
import com.thanosfisherman.mayi.listeners.multi.PermissionResultMultiListener;
import com.thanosfisherman.mayi.listeners.multi.RationaleMultiListener;

import java.io.PrintWriter;

public class MainActivity extends AppCompatActivity {


    private ProgressBar progressBar;
    private TextView textView;

    private String fileName = "contacts.txt";
    private Button start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Mayi.withActivity(MainActivity.this)
                        .withPermissions(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .onRationale(new RationaleMultiListener() {
                            @Override
                            public void onRationale(@NonNull PermissionBean[] permissions, @NonNull PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        })
                        .onResult(new PermissionResultMultiListener() {
                            @Override
                            public void permissionResults(@NonNull PermissionBean[] permissions) {
                                for (PermissionBean bean :
                                        permissions) {
                                    if (!bean.isGranted())
                                        return;
                                }
                                doImportContacts();
                            }
                        })
                        .check();
            }
        });
    }

    private void doImportContacts(){
        start.setVisibility(View.GONE);
        new Thread(){
            String TAG = "ContactImporter";

            void deleteAllContacts(){
                ContentResolver contentResolver = getContentResolver();
                Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
                while (cursor.moveToNext()) {
                    String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                    Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                    contentResolver.delete(uri, null, null);
                }
            }

            @Override
            public void run() {
                postMessage("Deleting all contacts...");
                deleteAllContacts();
                postMessage("All contacts has been deleted!");
//                postMessage("Importing contacts...");
//                InputStream inputStream = getResources().openRawResource(R.raw.contacts);
//                InputStreamReader streamReader=new InputStreamReader(inputStream);
//                BufferedReader reader=new BufferedReader(streamReader);
//
//                PrintWriter writer = null;
//
//                String line;
//                try {
//                    File vcfDest=new File(Environment.getExternalStorageDirectory(), "contacts_generated.vcf");
//                    if(vcfDest.exists()){
//                        vcfDest.delete();
//                    }
//                    vcfDest.getParentFile().mkdirs();
//                    vcfDest.createNewFile();
//
//                    FileOutputStream outputStream=new FileOutputStream(vcfDest);
//                    writer=new PrintWriter(outputStream);
//
////                    ArrayList<ContentProviderOperation> ops = new ArrayList<>();
////                    ContentResolver contentResolver=getContentResolver();
//                    int count = 0;
//                    while ((line = reader.readLine())!=null){
//                        count ++;
//                        Log.i(TAG, "importing number: "+line);
//
//                        write(writer, line, "Contact#"+count);
//
//
////                        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
////                                .withValue(ContactsContract.Data.RAW_CONTACT_ID, 0)
////                                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
////                                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, line)
////                                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, "contact #"+count)
////                                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
////                                .build());
//
//
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//                            if(isDestroyed()){
////                                ops.clear();
//                                break;
//                            }
//                        }
//                        if(isFinishing()){
////                            ops.clear();
//                            break;
//                        }
//
//                        // insert 100 by 100
////                        if(ops.size() >= 100){
////                            ContentProviderResult[] results = contentResolver.applyBatch(ContactsContract.AUTHORITY, ops);
////                            ops=new ArrayList<>();
////                            Log.i(TAG, "saved "+count+" contacts!\nresult: "+ Arrays.toString(results));
////                        }
//                    }
////                    if(ops.size() > 0){
////                        contentResolver.applyBatch(ContactsContract.AUTHORITY, ops);
////                    }
//                    postMessage("All Done!\nImported "+count+" contacts");
//                }catch (Throwable error){
//                    postMessage("Error While Reading Text or Writing Contacts\n"+error.getMessage());
//                }finally {
//                    try {
//                        reader.close();
//                    } catch (IOException ignored) {}
//                    if(writer!=null)
//                        writer.close();
//                }
            }

            void write(PrintWriter writer, String number, String name){
                String header = "BEGIN:VCARD\nVERSION:2.1\n";
                String nameSection = "N:"+name+";;;\nFN:"+name+"\n";
                String numberSection = "TEL;CELL:+98"+number+"\n";
                String footer = "END:VCARD\n";

                writer.write(header);
                writer.write(nameSection);
                writer.write(numberSection);
                writer.write(footer);
            }

            void postMessage(final CharSequence msg){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(msg);
                    }
                });
            }
        }.start();
    }

    private void initView() {
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.textView);
        start = findViewById(R.id.start);
    }
}
