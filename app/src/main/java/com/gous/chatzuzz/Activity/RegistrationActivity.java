package com.gous.chatzuzz.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gous.chatzuzz.R;
import com.gous.chatzuzz.ModelClass.Users;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegistrationActivity extends  AppCompatActivity {
    TextView txt_signin,btn_SignUp;
    CircleImageView profile_image;
    EditText reg_name,reg_email,reg_pass,reg_cpass;
    FirebaseAuth auth;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    Uri imageUri;
    FirebaseDatabase Database;
    FirebaseStorage Storage;
    String imageURI;
    ProgressDialog progressDialog;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);


        auth=FirebaseAuth.getInstance();
        Database=FirebaseDatabase.getInstance();
        Storage=FirebaseStorage.getInstance();

        txt_signin=findViewById(R.id.txt_signin);
        profile_image=findViewById(R.id.profile_image);
        reg_email=findViewById(R.id.reg_email);
        reg_name=findViewById(R.id.reg_name);
        reg_pass=findViewById(R.id.reg_pass);
        reg_cpass=findViewById(R.id.reg_cpass);
        btn_SignUp=findViewById(R.id.btn_SignUp);

        btn_SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                progressDialog.show();
                String name=reg_name.getText().toString();
                String email=reg_email.getText().toString();
                String pass=reg_pass.getText().toString();
                String cpass=reg_cpass.getText().toString();
                String status="Hey I am using this App";



                if(TextUtils.isEmpty(name)|| TextUtils.isEmpty(email)||TextUtils.isEmpty(pass)||TextUtils.isEmpty(cpass))
                {
                    progressDialog.dismiss();

                    Toast.makeText(RegistrationActivity.this,"Please enter valid data",Toast.LENGTH_SHORT).show();
                }else if(!email.matches(emailPattern))
                {
                    reg_email.setError("please enter valid email");
                    progressDialog.dismiss();

                    Toast.makeText(RegistrationActivity.this,"Please enter valid email",Toast.LENGTH_SHORT).show();
                }else if(!pass.equals(cpass))
                {
                    progressDialog.dismiss();
                    Toast.makeText(RegistrationActivity.this,"password doesnot match",Toast.LENGTH_SHORT).show();
                }else if (pass.length()<6)
                {
                    progressDialog.dismiss();
                    Toast.makeText(RegistrationActivity.this,"Enter 6 character password",Toast.LENGTH_SHORT).show();
                }else {
                    auth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){

                                DatabaseReference reference=Database.getReference().child("user").child(auth.getUid());
                                StorageReference storageReference=Storage.getReference().child("upload").child(auth.getUid());

                                if(imageUri!=null) {
                                    storageReference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                storageReference.getDownloadUrl().addOnSuccessListener(new  OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        imageURI=uri.toString();
                                                        Users users=new Users(auth.getUid(),name,email,imageURI,status);
                                                        reference.setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    startActivity(new Intent(RegistrationActivity.this, HomeActivity.class));
                                                                }else{
                                                                    Toast.makeText(RegistrationActivity.this,"Error in Creation",Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                    }
                                                });
                                            }

                                        }
                                    });
                                }else {
                                    String status="Hey I am using this App";
                                    imageURI="https://firebasestorage.googleapis.com/v0/b/chatzuzz.appspot.com/o/profilepic.jpg?alt=media&token=c4e89e6f-dcd8-467d-9306-d98dee09ac02";
                                    Users users=new Users(auth.getUid(),name,email,imageURI,status);
                                    reference.setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                progressDialog.dismiss();
                                                startActivity(new Intent(RegistrationActivity.this,HomeActivity.class));
                                            }else{
                                                Toast.makeText(RegistrationActivity.this,"Error in Creation",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            }else{
                                progressDialog.dismiss();
                                Toast.makeText(RegistrationActivity.this,"Something went Wrong",Toast.LENGTH_SHORT).show();

                            }
                        }
                    });

                }
            }
        });

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 10);
            }
        });

        txt_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==10)
        {
            if (data!=null)
            {
                imageUri=data.getData();
                profile_image.setImageURI(imageUri);
            }
        }
    }
}