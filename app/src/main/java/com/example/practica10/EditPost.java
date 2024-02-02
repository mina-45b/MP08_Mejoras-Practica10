package com.example.practica10;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditPost extends Fragment {
    public AppViewModel appViewModel;
    NavController navController;

    Button updateButton;
    EditText nuevoContentPost;

    Post postSeleccionado;

    static String postId;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        appViewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);

        updateButton = view.findViewById(R.id.updateButton);
        nuevoContentPost = view.findViewById(R.id.nuevoContentPost);

        postSeleccionado = appViewModel.postSeleccionado.getValue();

        if (postSeleccionado != null) {
            nuevoContentPost.setText(postSeleccionado.content);
        }

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rePublicar();
            }
        });
    }

    public void rePublicar() {
        String nvpostContent = nuevoContentPost.getText().toString();
        if (TextUtils.isEmpty(nvpostContent)) {
            nuevoContentPost.setError("Required");
            return;
        }
        updateButton.setEnabled(false);
        actualizarPost(postSeleccionado, nvpostContent);
    }

    private void actualizarPost(Post post, String nuevoContenido) {
        // Verificar que el post y el nuevo contenido no sean nulos
        if (post != null && nuevoContenido != null) {

            // Crear un nuevo mapa con los campos que deseas actualizar
            Map<String, Object> actualizaciones = new HashMap<>();
            actualizaciones.put("content", nuevoContenido);

            // Obtener la marca de tiempo actual
            long nuevoTimeStamp = System.currentTimeMillis();
            actualizaciones.put("timeStamp", nuevoTimeStamp);

            // Actualizar el contenido del post en Firebase
            FirebaseFirestore.getInstance().collection("posts")
                    .document(postId)
                    .update(actualizaciones)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Operación de actualización exitosa
                            // Puedes realizar cualquier acción adicional, como mostrar un mensaje
                            Toast.makeText(requireContext(), "Post actualizado con éxito", Toast.LENGTH_SHORT).show();

                            // Navegar de vuelta a la pantalla de inicio u otra pantalla deseada
                            navController.popBackStack();  // O utiliza la navegación que necesites
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Operación de actualización fallida
                            // Puedes manejar cualquier error, como mostrar un mensaje de error
                            Toast.makeText(requireContext(), "Error al actualizar el post", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public static void obtenerPostKey(String postKey) {
        postId = postKey;
    }

}