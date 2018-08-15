package com.linminitools.mysync;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyProperties;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Calendar;

import javax.security.auth.x500.X500Principal;

import static com.linminitools.mysync.MainActivity.appContext;
import static com.linminitools.mysync.MainActivity.getPath;
import static java.nio.CharBuffer.wrap;

public class configurationTypeSelector extends AppCompatActivity {
    customAdapter adapter;
    addDaemonConfig d;
    addRemoteShellConfig r;
    addAdvancedConfig a;
    ExpandableListView lv_config_types;
    int latest_group_clicked;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config_types);

        ArrayList<String> Headers = new ArrayList();

        Headers.add("Rsync Daemon Mode");
        Headers.add("Rsync Remote Shell");
        Headers.add("Rsync Custom Configuration (Advanced)");

        lv_config_types = findViewById(R.id.lv_config_types);
        adapter = new customAdapter(this,Headers,21);

        lv_config_types.setAdapter((ExpandableListAdapter) adapter);

        lv_config_types.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                for (int i=0;i<2;i++){
                    parent.collapseGroup(i);
                }
                if (!parent.isGroupExpanded(groupPosition)) parent.expandGroup(groupPosition);
                else parent.collapseGroup(groupPosition);
                latest_group_clicked=groupPosition;
                return true;
            }
        });

        //Log.d("ISNULL", r.toString());

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request it is that we're responding to
        Log.d("ONACTIVITYRESULT",String.valueOf(requestCode)+" "+String.valueOf(resultCode));
        if (resultCode == RESULT_OK ) {
            // Make sure the request was successful

            // Get the URI that points to the selected path

            Uri pathUri = data.getData();
            Uri dirUri = DocumentsContract.buildDocumentUriUsingTree(pathUri, DocumentsContract.getTreeDocumentId(pathUri));
            String local_path = getPath(this, dirUri);

            if (requestCode == 0) {

                SharedPreferences path_prefs = getSharedPreferences("Rsync_Config_path", MODE_PRIVATE);
                SharedPreferences.Editor path_prefseditor = path_prefs.edit();
                path_prefseditor.putString("local_path", local_path);
                path_prefseditor.apply();

                //Log.d("INTENT_DATA",String.valueOf(data.getIntExtra("mode",-1)));

                if (latest_group_clicked==0) {
                    d = (addDaemonConfig) adapter.getChild(0, 0);
                    d.Update_view(local_path);
                }
                else if (latest_group_clicked==1){
                    r = (addRemoteShellConfig) adapter.getChild(1,0);
                    r.Update_view(local_path);

                }
                //adapter.notifyDataSetChanged();
            }
            else if(requestCode==1) ((EditText)findViewById(R.id.et_arg1_field)).append(local_path);
            else if(requestCode==2) ((EditText)findViewById(R.id.et_arg2_field)).append(local_path);


        }

    }

    public void addPath(View v){
        Uri selectedUri = Uri.parse(Environment.getDataDirectory().toString());
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        Intent i2= Intent.createChooser(i,"Choose Directory");
        String from_button= (String)v.getTag(R.id.ib_arg1_path_selector);
        int r=0;

        if (from_button=="arg1") r=1;
        else if (from_button=="arg2") r=2;

        startActivityForResult(i2,r);
    }


    public void viewCommand(View v){
        if (latest_group_clicked==0) {
            if (d == null) d = (addDaemonConfig) adapter.getChild(0, 0);
            d.viewCommand(v);
        }
        else if (latest_group_clicked==1){
            if (r== null) r = (addRemoteShellConfig) adapter.getChild(1,0);
            r.viewCommand(v);
        }
        else if (latest_group_clicked==2){
            if (a==null) a= (addAdvancedConfig) adapter.getChild(2,0);
            a.viewCommand(v);
        }
    }

    public void saveConfig(View v) {
        if (latest_group_clicked==0) {
            if (d == null) d = (addDaemonConfig) adapter.getChild(0, 0);
            if (d.saveConfig(v)) this.finish();
        }
        else if (latest_group_clicked==1){
            if (r == null) r = (addRemoteShellConfig) adapter.getChild(1,0);
            if (r.saveConfig(v)) this.finish();
        }
        else if (latest_group_clicked==2){
            if (a == null) a = (addAdvancedConfig) adapter.getChild(2,0);
            if (a.saveConfig(v)) this.finish();
        }

    }

    public void rsync_help(View v){
        AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Rsync Options Help")
                        .setMessage(getResources().getString(R.string.rsync_options))
                        .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                ;
        alertDialogBuilder.show();
    }

    public void generate_keys(View v){
        String k="";
        String pem="";
        try {

            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            String KEY_ALIAS = "SSH_CREDENTIALS";

            // Generate the RSA key pairs
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                // Generate a key pair for encryption
                Log.d("SSH", "ALIAS EXISTS");
                Calendar start = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                end.add(Calendar.YEAR, 30);

                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(appContext)
                        .setAlias(KEY_ALIAS)
                        .setSubject(new X500Principal("CN=" + KEY_ALIAS))
                        .setSerialNumber(BigInteger.TEN)
                        .setStartDate(start.getTime())
                        .setKeyType(KeyProperties.KEY_ALGORITHM_RSA)
                        .setEndDate(end.getTime())
                        .build();
                KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
                kpg.initialize(spec);
                KeyPair kp = kpg.generateKeyPair();
                byte publickey[] = kp.getPublic().getEncoded();

                KeyFactory fact = KeyFactory.getInstance("RSA");
                X509EncodedKeySpec sp = fact.getKeySpec(kp.getPublic(), X509EncodedKeySpec.class);
                String publicKeyString = Base64.encodeToString(sp.getEncoded(), Base64.DEFAULT);
                String pub= Base64.encodeToString(publickey,Base64.DEFAULT);
                pem = "-----BEGIN PUBLIC KEY-----\n" + wrap(pub) + "-----END PUBLIC KEY-----\n";

            }

            else{
                PublicKey publicKey= keyStore.getCertificate(KEY_ALIAS).getPublicKey();
                byte publickey[] = keyStore.getCertificate(KEY_ALIAS).getPublicKey().getEncoded();

                String pub= Base64.encodeToString(publickey,Base64.DEFAULT);
                pem = "-----BEGIN PUBLIC KEY-----\n" + wrap(pub) + "-----END PUBLIC KEY-----\n";

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        k=pem;

        String public_key_path=appContext.getApplicationInfo().dataDir + "/rsa_key.pub";

        File rsa_pub = new File(public_key_path);
        try {
            FileWriter fw = new FileWriter(rsa_pub);
            fw.write(k);
            fw.close();
            AlertDialog dialog = new AlertDialog.Builder(v.getContext())
                    .setTitle("KeyPair Generation")
                    .setMessage("The location of the public key is \n "+public_key_path+" \nThis file must be imported in your server")
                    .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();

        }
        catch( IOException e){
            Log.d("IOEexcepton",e.toString());
        }


    }


}
