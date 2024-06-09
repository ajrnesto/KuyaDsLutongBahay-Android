package com.kuya_d.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kuya_d.Dialogs.MenuItemDialog;
import com.kuya_d.Dialogs.ShopItemDialog;
import com.kuya_d.Objects.ShopItem;
import com.kuya_d.R;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class MenuItemAdapter extends RecyclerView.Adapter<MenuItemAdapter.menuItemViewHolder> {

    private final StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    Context context;
    ArrayList<ShopItem> arrMenuItem;
    private OnMenuItemClicked mOnMenuItemClicked;

    public MenuItemAdapter(Context context, ArrayList<ShopItem> arrMenuItem, OnMenuItemClicked mOnMenuItemClicked) {
        this.context = context;
        this.arrMenuItem = arrMenuItem;
        this.mOnMenuItemClicked = mOnMenuItemClicked;
    }

    @NonNull
    @Override
    public menuItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_menu_item, parent, false);
        return new menuItemViewHolder(view, mOnMenuItemClicked);
    }

    @Override
    public void onBindViewHolder(@NonNull menuItemViewHolder holder, int position) {
        ShopItem menuItem = arrMenuItem.get(position);

        String categoryId = menuItem.getCategoryId();
        String productName = menuItem.getProductName();
        String productDetails = menuItem.getProductDetails();
        Long thumbnail = menuItem.getThumbnail();

        DecimalFormat df = new DecimalFormat("0.00");
        holder.tvName.setText(productName);
        holder.tvDetails.setText(productDetails);

        storageRef.child("products/"+thumbnail).getDownloadUrl()
                .addOnSuccessListener(uri -> Picasso.get().load(uri).resize(100,0).centerCrop().into(holder.ivProduct))
                .addOnFailureListener(e -> Picasso.get().load("https://via.placeholder.com/150?text=No+Image").fit().into(holder.ivProduct));
    }

    @Override
    public int getItemCount() {
        return arrMenuItem.size();
    }

    public class menuItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        OnMenuItemClicked onMenuItemClicked;
        MaterialCardView cvContainer;
        RoundedImageView ivProduct;
        TextView tvName, tvDetails;

        public menuItemViewHolder(@NonNull View itemView, OnMenuItemClicked onMenuItemClicked) {
            super(itemView);

            cvContainer = itemView.findViewById(R.id.cvContainer);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvName = itemView.findViewById(R.id.tvName);
            tvDetails = itemView.findViewById(R.id.tvDetails);

            this.onMenuItemClicked = onMenuItemClicked;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onMenuItemClicked.onMenuItemClicked(getAdapterPosition());

            Bundle args = new Bundle();
            args.putString("id", arrMenuItem.get(getAdapterPosition()).getId());
            args.putString("productName", arrMenuItem.get(getAdapterPosition()).getProductName());
            args.putString("productDetails", arrMenuItem.get(getAdapterPosition()).getProductDetails());
            args.putString("category", arrMenuItem.get(getAdapterPosition()).getCategoryId());
            args.putLong("thumbnail", arrMenuItem.get(getAdapterPosition()).getThumbnail());

            MenuItemDialog menuItemDialog = new MenuItemDialog();
            menuItemDialog.setArguments(args);
            menuItemDialog.show(((FragmentActivity)context).getSupportFragmentManager(), "MENU_ITEM_DIALOG");
        }
    }

    public interface OnMenuItemClicked{
        void onMenuItemClicked(int position);
    }
}
