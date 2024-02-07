package com.example.practica10;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class HomeFragment extends Fragment {
    public AppViewModel appViewModel;
    NavController navController;

    RecyclerView postsRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        appViewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);

        view.findViewById(R.id.gotoNewPostFragmentButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.newPostFragment);
            }
        });

         postsRecyclerView = view.findViewById(R.id.postsRecyclerView);

        Query query = getQuery();
        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class)
                .setLifecycleOwner(getViewLifecycleOwner())
                .build();

        postsRecyclerView.setAdapter(new PostsAdapter(options));
    }

    Query getQuery()
    {
        return FirebaseFirestore.getInstance().collection("posts").orderBy("timeStamp", Query.Direction.DESCENDING).limit(50);
    }

    public class PostsAdapter extends FirestoreRecyclerAdapter<Post, PostsAdapter.PostViewHolder> {
        public PostsAdapter(@NonNull FirestoreRecyclerOptions<Post> options) {super(options);}

        

        @NonNull
        @Override
        public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PostViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_post, parent, false));
        }

        @Override
        protected void onBindViewHolder(@NonNull PostViewHolder holder, int position, @NonNull final Post post) {


    



            if (post.authorPhotoUrl == null) {
                holder.authorPhotoImageView.setImageResource(R.drawable.user);
                

            } else {
                Glide.with(getContext()).load(post.authorPhotoUrl).circleCrop().into(holder.authorPhotoImageView);
            }

            holder.authorTextView.setText(post.author);
            holder.contentTextView.setText(post.content);

            // Gestion de likes
                final String postKey = getSnapshots().getSnapshot(position).getId();
                final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                if(post.likes.containsKey(uid))
                    holder.likeImageView.setImageResource(R.drawable.like_on);
                else
                    holder.likeImageView.setImageResource(R.drawable.like_off);
                holder.numLikesTextView.setText(String.valueOf(post.likes.size()));
                holder.likeImageView.setOnClickListener(view -> {
                    FirebaseFirestore.getInstance().collection("posts")
                            .document(postKey)
                            .update("likes."+uid, post.likes.containsKey(uid) ?
                                    FieldValue.delete() : true);
                });


            // Miniatura de media
            if (post.mediaUrl != null) {
                holder.mediaImageView.setVisibility(View.VISIBLE);
                if ("audio".equals(post.mediaType)) {
                    Glide.with(requireView()).load(R.drawable.audio).centerCrop().into(holder.mediaImageView);
                } else {
                    Glide.with(requireView()).load(post.mediaUrl).centerCrop().into(holder.mediaImageView);
                }
                holder.mediaImageView.setOnClickListener(view -> { appViewModel.postSeleccionado.setValue(post);
                    navController.navigate(R.id.mediaFragment);
                });
            } else {
                holder.mediaImageView.setVisibility(View.GONE);
            }

            //Fecha y Hora
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm  dd/MM/yyyy");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(post.timeStamp);

            holder.timeTextView.setText(formatter.format(calendar.getTime()));

            //Eliminar post
            if (post.uid.equals(uid)) {
                holder.deleteImageView.setVisibility(View.VISIBLE);

                holder.deleteImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // documento del post y elimínalo
                        FirebaseFirestore.getInstance().collection("posts")
                                .document(postKey)
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Operación de eliminación exitosa
                                        // Aquí puedes realizar cualquier acción adicional, como mostrar un mensaje
                                        Toast.makeText(getContext(), "Post eliminado con éxito", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Operación de eliminación fallida
                                        // Aquí puedes manejar cualquier error, como mostrar un mensaje de error
                                        Toast.makeText(getContext(), "Error al eliminar el post", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });

            } else {
                holder.deleteImageView.setVisibility(View.GONE);
            }

            //Editar post
            if (post.uid.equals(uid)) {
                holder.editImageView.setVisibility(View.VISIBLE);

                holder.editImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        appViewModel.postSeleccionado.setValue(post);
                        EditPost.obtenerPostKey(postKey);

                        navController.navigate(R.id.editPost);
                    }
                });

            } else {

                holder.editImageView.setVisibility(View.GONE);
            }

            //Marcar post
            if(post.bookMarks.containsKey(uid))
                holder.bookmarkImageView.setImageResource(R.drawable.bookmark_on);
            else
                holder.bookmarkImageView.setImageResource(R.drawable.bookmark);

            holder.bookmarkImageView.setOnClickListener(view -> {

                FirebaseFirestore.getInstance().collection("favorites").document(uid).collection("posts").whereEqualTo("postId", post.postId)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Verificar si se encontraron documentos
                                if (task.getResult().isEmpty()) {
                                    // No se encontraron documentos que cumplan con el criterio de la consulta
                                    FirebaseFirestore.getInstance().collection("favorites")
                                            .document(uid)
                                            .collection("posts")
                                            .add(post)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {

                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // La actualización en la base de datos falló
                                                    // Manejar el error según sea necesario
                                                }
                                            });
                                    System.out.println("No se encontraron posts con postId igual a " + post.postId);
                                } else {
                                    // Se encontraron documentos que cumplen con el criterio de la consulta
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        // Obtenemos la referencia al documento y lo eliminamos
                                        FirebaseFirestore.getInstance().collection("favorites").document(uid).collection("posts").document(document.getId())
                                                .delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    // Manejar el éxito de la eliminación
                                                    System.out.println("Documento eliminado correctamente.");
                                                })
                                                .addOnFailureListener(e -> {
                                                    // Manejar el fallo en la eliminación
                                                    System.err.println("Error al eliminar el documento: " + e.getMessage());
                                                });
                                    }
                                }
                            } else {
                                // Manejar errores en la ejecución de la consulta
                                Exception exception = task.getException();
                                System.err.println("Error al ejecutar la consulta: " + exception.getMessage());
                            }
                        });

                FirebaseFirestore.getInstance().collection("posts")
                        .document(postKey)
                        .update("bookMarks."+uid, post.bookMarks.containsKey(uid) ?
                                FieldValue.delete() : true);
            });

        }

        public class PostViewHolder extends RecyclerView.ViewHolder {
            ImageView authorPhotoImageView, likeImageView, mediaImageView, deleteImageView, editImageView, bookmarkImageView;
            TextView authorTextView, contentTextView, numLikesTextView, timeTextView;
            PostViewHolder(@NonNull View itemView) {
                super(itemView);
                authorPhotoImageView =
                        itemView.findViewById(R.id.photoImageView);
                likeImageView = itemView.findViewById(R.id.likeImageView);
                mediaImageView = itemView.findViewById(R.id.mediaImage);
                authorTextView = itemView.findViewById(R.id.authorTextView);
                contentTextView = itemView.findViewById(R.id.contentTextView);
                numLikesTextView = itemView.findViewById(R.id.numLikesTextView);
                timeTextView = itemView.findViewById(R.id.timeTextView);
                deleteImageView = itemView.findViewById(R.id.delete);
                editImageView = itemView.findViewById(R.id.edit);
               bookmarkImageView = itemView.findViewById(R.id.bookMark);
            }
        }
    }
}
