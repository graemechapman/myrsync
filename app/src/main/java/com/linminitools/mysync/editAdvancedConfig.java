package com.linminitools.mysync;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.x500.X500Principal;

import static com.linminitools.mysync.MainActivity.appContext;
import static com.linminitools.mysync.MainActivity.configs;
import static com.linminitools.mysync.MainActivity.getPath;
import static java.nio.CharBuffer.wrap;

public class editAdvancedConfig extends AppCompatActivity {

    EditText options_field, arg1_field, arg2_field;
    String options;
    TextView view_cmd;
    CheckBox cb_useLogFile;
    Button save, generate_ssh, view_cmd_bt, execute_bt;
    ImageButton info, ssh_keys_help, arg1_path_selector, arg2_path_selector;
    LayoutInflater inflater;
    View vi = null;
    Context context;
    RS_Configuration config;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int pos = getIntent().getIntExtra("pos", -1);
        config = configs.get(pos);
        context = getBaseContext();
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        vi = inflater.inflate(R.layout.add_advanced_config, (ViewGroup) findViewById(android.R.id.content), false);
        options_field = vi.findViewById(R.id.et_options_field);
        arg1_field = vi.findViewById(R.id.et_arg1_field);
        arg2_field = vi.findViewById(R.id.et_arg2_field);

        arg1_path_selector = vi.findViewById(R.id.ib_arg1_path_selector);
        arg2_path_selector = vi.findViewById(R.id.ib_arg2_path_selector);

        info = vi.findViewById(R.id.ib_info);
        save = vi.findViewById(R.id.bt_save);
        view_cmd_bt = vi.findViewById(R.id.bt_view);
        ssh_keys_help = vi.findViewById(R.id.ib_keys_help);
        execute_bt = vi.findViewById(R.id.bt_execute);

        cb_useLogFile = vi.findViewById(R.id.cb_logfile);

        view_cmd = vi.findViewById(R.id.tv_rs_cmd_View);

        generate_ssh = vi.findViewById(R.id.bt_generate_ssh_keys);

        execute_bt.setEnabled(true);

        arg1_path_selector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPath(v,1);
            }
        });

        arg2_path_selector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPath(v,2);
            }
        });


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveConfig(v);
                finish();
            }
        });

        execute_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                config.executeConfig(context);
                finish();
            }
        });

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                info_message(v);
            }
        });

        ssh_keys_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ssh_help_dialog(v);
            }
        });

        generate_ssh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String k;
                k=generate_keys(v);
                File rsa_pub = new File(appContext.getApplicationInfo().dataDir + "/rsa_key.pub");
                try {
                    FileWriter fw = new FileWriter(rsa_pub);
                    fw.write(k);
                    fw.close();
                }
                catch( IOException e){
                    Log.d("IOEexcepton",e.toString());
                }
            }
        });


        if (pos != -1) {
            options_field.setText(config.rs_options);
            arg1_field.setText(config.arg1);
            arg2_field.setText(config.arg2);
        }

        setContentView(vi);

    }

    public editAdvancedConfig() {
    }

    public String generate_keys(View v) {
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

                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
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
                String pub = Base64.encodeToString(publickey, Base64.DEFAULT);
                String pem = "-----BEGIN PUBLIC KEY-----\n" + wrap(pub) + "-----END PUBLIC KEY-----\n";
                return pem;
            } else {
                PublicKey publicKey = keyStore.getCertificate(KEY_ALIAS).getPublicKey();
                byte publickey[] = keyStore.getCertificate(KEY_ALIAS).getPublicKey().getEncoded();

                String pub = Base64.encodeToString(publickey, Base64.DEFAULT);
                String pem = "-----BEGIN PUBLIC KEY-----\n" + wrap(pub) + "-----END PUBLIC KEY-----\n";
                return pem;
            }








            /*

            AlertDialog.Builder alertDialogBuilder =
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Your KEYS")
                            .setMessage("I want to show the keys here")
                            .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                    ;
            alertDialogBuilder.show();



            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyStore.getDefaultType());
            keyPairGenerator.initialize(2048);
            KeyPair kp= keyPairGenerator.generateKeyPair();

            PrivateKey priv= kp.getPrivate();
            PublicKey pub= kp.getPublic();

            KeyStore ks= KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null);

            KeyStore.ProtectionParameter protParam = new KeyStore.PasswordProtection(priv.getEncoded());

            KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) ks.getEntry("privateKey", pivKey);

            ks.setEntry("PrivateKey",(KeyStore.PrivateKeyEntry)priv,null);
                */
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected Map<String, String> processForm(View v) {
        if (vi == null) {
            if (inflater == null)
                inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            vi = inflater.inflate(R.layout.add_advanced_config, (ViewGroup) findViewById(android.R.id.content), false);
        }

        Map<String, String> configMap = new HashMap<String, String>();

        options = String.valueOf(options_field.getText());

        if (options == "-") {
            options = "";
        }

        configMap.put("rs_arg1", String.valueOf(arg1_field.getText()));
        configMap.put("rs_arg2", String.valueOf(arg2_field.getText()));
        configMap.put("rs_options", options);
        configMap.put("rs_mode", "2");

        return configMap;
    }

    public boolean saveConfig(View v) {
        Log.d("SAVE-CONFIG", "Clicked");
        Map<String, String> configMap = this.processForm(v);

        String rs_options = configMap.get("rs_options");
        String arg1 = configMap.get("rs_arg1");
        String arg2 = configMap.get("rs_arg2");

        final SharedPreferences prefs =  context.getSharedPreferences("Rsync_Command_build", MODE_PRIVATE);
        String log_path= prefs.getString("log",context.getApplicationInfo().dataDir+"/logfile.log");

        if (cb_useLogFile.isChecked()) rs_options=rs_options+"--log-file="+log_path;



        if (!arg1.isEmpty() && !arg2.isEmpty() && !rs_options.isEmpty()) {

            config.mode = 2;
            config.arg1 = arg1;
            config.arg2 = arg2;
            config.rs_options = rs_options;
            config.saveToDisk();
            configs.add(config);
            return true;

        } else {

            CharSequence text = "Configuration is not Complete!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(appContext, text, duration);
            toast.show();
            return false;
        }

    }

    public void viewCommand(View v) {

        Map<String, String> configMap = this.processForm(v);

        String options = configMap.get("rs_options");
        String arg1 = configMap.get("rs_arg1");
        String arg2 = configMap.get("rs_arg2");

        TextView tv = this.vi.findViewById(R.id.tv_rs_cmd_View);

        String Rsync_command = "rsync " + "-" + options + " " + arg1 + " " + arg2;
        tv.setText(Rsync_command);
    }

    public void info_message(View v) {
        final SpannableString s = new SpannableString(getResources().getString(R.string.adv_config_help));
        Linkify.addLinks(s, Linkify.WEB_URLS);

        AlertDialog dialog = new AlertDialog.Builder(v.getContext())
                        .setTitle("Advanced Configuration Help")
                        .setMessage(s)
                        .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
        TextView textView = dialog.findViewById(android.R.id.message);

        textView.setTextSize(12);
        textView.setMovementMethod(LinkMovementMethod.getInstance());


    }

    public void addPath(View v, int request) {
        Uri selectedUri = Uri.parse(Environment.getDataDirectory().toString());
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        Intent i2 = Intent.createChooser(i, "Choose Directory");

        startActivityForResult(i2, request);
    }

    public void ssh_help_dialog(View v){
        final SpannableString s = new SpannableString(getResources().getString(R.string.ssh_help));
        Linkify.addLinks(s, Linkify.ALL);

        AlertDialog dialog = new AlertDialog.Builder(v.getContext())
                .setTitle("SSH Keys Help")
                .setMessage(s)
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request it is that we're responding to
        if (resultCode == RESULT_OK){
            Uri pathUri = data.getData();
            Uri dirUri = DocumentsContract.buildDocumentUriUsingTree(pathUri, DocumentsContract.getTreeDocumentId(pathUri));
            String local_path = getPath(this, dirUri);
            if (requestCode == 1) arg1_field.append(local_path);
            else if (requestCode==2) arg2_field.append(local_path);
        }
    }




}
