package com.example.practica10;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class BookMarksFragment extends HomeFragment {



    @Override
    Query getQuery() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        return FirebaseFirestore.getInstance().collection("favorites").document(uid).collection("posts").limit(50);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.gotoNewPostFragmentButton).setVisibility(View.GONE);

        // Obtener el adaptador de la RecyclerView
        PostsAdapter postsAdapter = (PostsAdapter) postsRecyclerView.getAdapter();

        // Ocultar likes y bookmarks
        if (postsAdapter != null) {
            postsAdapter.setShowLikesAndBookmarks(false);
            postsAdapter.notifyDataSetChanged();
        }

    }


}
