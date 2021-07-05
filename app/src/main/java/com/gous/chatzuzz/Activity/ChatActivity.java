package com.gous.chatzuzz.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.gous.chatzuzz.Adapter.MessagesAdapter;
import com.gous.chatzuzz.ModelClass.Messages;
import com.gous.chatzuzz.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    String RecieverImage,RecieverUID,RecieverName,SenderUID;
    CircleImageView profileImage;
    TextView recieverName;
    FirebaseDatabase database;
    FirebaseAuth firebaseAuth;
    public  static String sImage;
    public  static String rImage;

    CardView sendBtn;
    EditText edtMessage;

    String senderRoom,recieverRoom;
    RecyclerView messageAdapter;
    ArrayList<Messages> messagesArrayList;
    MessagesAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        database=FirebaseDatabase.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();

        RecieverName=getIntent().getStringExtra("name");
        RecieverImage=getIntent().getStringExtra("ReceiverImage");
        RecieverUID=getIntent().getStringExtra("uid");
        messagesArrayList=new ArrayList<>();

        profileImage=findViewById(R.id.profile_image);
        recieverName=findViewById(R.id.reciverName);
        messageAdapter=findViewById(R.id.messageAdapter);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messageAdapter.setLayoutManager(linearLayoutManager);
        adapter=new MessagesAdapter(ChatActivity.this, messagesArrayList);
        messageAdapter.setAdapter(adapter);

        sendBtn=findViewById(R.id.sendBtn);
        edtMessage=findViewById(R.id.edtMessage);

        Picasso.get().load(RecieverImage).into(profileImage);
        recieverName.setText(""+RecieverName);
        SenderUID=firebaseAuth.getUid();

        senderRoom=SenderUID+RecieverUID;
        recieverRoom=RecieverUID+SenderUID;

        DatabaseReference reference=database.getReference().child("user").child(firebaseAuth.getUid());
        DatabaseReference chatReference=database.getReference().child("chats").child(senderRoom).child("messages");


        chatReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                messagesArrayList.clear();

                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Messages messages=dataSnapshot.getValue(Messages.class);
                    messagesArrayList.add(messages);
                }
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                sImage=snapshot.child("imageUri").getValue().toString();
                rImage=RecieverImage;
            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message=edtMessage.getText().toString();
                if(message.isEmpty()) {
                    Toast.makeText(ChatActivity.this, "Please enter Valid Message", Toast.LENGTH_SHORT).show();
                    return;
                }
                edtMessage.setText("");
                Date date=new Date();
                Messages messages=new Messages(message,SenderUID,date.getTime());
                database=FirebaseDatabase.getInstance();
                database.getReference().child("chats").
                        child(senderRoom)
                        .child("messages")
                        .push()
                        .setValue(messages)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                database.getReference().child("chats").
                                        child(recieverRoom)
                                        .child("messages")
                                        .push()
                                        .setValue(messages)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                            }
                                        });
                            }
                        });









            }
        });







    }
}