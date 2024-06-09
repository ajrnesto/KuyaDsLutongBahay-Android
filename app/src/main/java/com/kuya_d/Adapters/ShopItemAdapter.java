package com.kuya_d.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kuya_d.Objects.ShopItem;
import com.kuya_d.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class ShopItemAdapter extends RecyclerView.Adapter<ShopItemAdapter.shopItemViewHolder> {

    private final FirebaseFirestore DB = FirebaseFirestore.getInstance();
    private final StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    Context context;
    ArrayList<ShopItem> arrShopItem;
    ArrayList<String> arrSelectedItemsId;
    private OnShopItemListener mOnShopItemListener;

    public ShopItemAdapter(Context context, ArrayList<ShopItem> arrShopItem, ArrayList<String> arrSelectedItemsId, OnShopItemListener onShopItemListener) {
        this.context = context;
        this.arrShopItem = arrShopItem;
        this.arrSelectedItemsId = arrSelectedItemsId;
        this.mOnShopItemListener = onShopItemListener;
    }

    @NonNull
    @Override
    public shopItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_shop_item, parent, false);
        return new shopItemViewHolder(view, mOnShopItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull shopItemViewHolder holder, int position) {
        ShopItem shopItem = arrShopItem.get(position);

        String categoryId = shopItem.getCategoryId();
        String productName = shopItem.getProductName();
        String productDetails = shopItem.getProductDetails();
        double price = shopItem.getPrice();
        DecimalFormat df = new DecimalFormat("0.00");
        holder.tvPrice.setText("₱"+df.format(price)+"/Head");

        Long thumbnail = shopItem.getThumbnail();

        if (arrSelectedItemsId.contains(shopItem.getId())) {
            holder.cvContainer.setCardBackgroundColor(context.getResources().getColor(R.color.blue_primary_variant));
            holder.tvName.setTextColor(context.getResources().getColor(R.color.white));
            holder.tvCategory.setTextColor(context.getResources().getColor(R.color.white));
            holder.tvDetails.setTextColor(context.getResources().getColor(R.color.white));
            holder.btnShowDescription.setTextColor(context.getResources().getColor(R.color.white));
            holder.btnShowDescription.setStrokeColorResource(R.color.white);
        }
        else {
            holder.cvContainer.setCardBackgroundColor(context.getResources().getColor(R.color.white));
            holder.tvName.setTextColor(context.getResources().getColor(R.color.blue_primary));
            holder.tvCategory.setTextColor(context.getResources().getColor(R.color.gray_dark));
            holder.tvDetails.setTextColor(context.getResources().getColor(R.color.gray_dark));
            holder.btnShowDescription.setTextColor(context.getResources().getColor(R.color.blue_primary));
            holder.btnShowDescription.setStrokeColorResource(R.color.blue_primary);
        }

        holder.tvName.setText(productName);
        DB.collection("categories").document(categoryId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            holder.tvCategory.setText(task.getResult().getString("categoryName"));
                        }
                    }
                });
        holder.tvDetails.setText(productDetails);
        storageRef.child("products/"+thumbnail).getDownloadUrl()
                .addOnSuccessListener(uri -> Picasso.get().load(uri).resize(240,0).centerCrop().into(holder.ivProduct))
                .addOnFailureListener(e -> Picasso.get().load("https://via.placeholder.com/150?text=No+Image").fit().into(holder.ivProduct));
    }

    @Override
    public int getItemCount() {
        return arrShopItem.size();
    }

    public class shopItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        OnShopItemListener onShopItemListener;
        MaterialCardView cvContainer;
        RoundedImageView ivProduct;
        TextView tvName, tvCategory, tvDetails, tvPrice;
        MaterialButton btnShowDescription;

        public shopItemViewHolder(@NonNull View itemView, OnShopItemListener onShopItemListener) {
            super(itemView);

            cvContainer = itemView.findViewById(R.id.cvContainer);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvName = itemView.findViewById(R.id.tvName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDetails = itemView.findViewById(R.id.tvDetails);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnShowDescription = itemView.findViewById(R.id.btnShowDescription);

            this.onShopItemListener = onShopItemListener;
            itemView.setOnClickListener(this);

            btnShowDescription.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (tvDetails.getVisibility() == View.GONE) {
                        tvDetails.setVisibility(View.VISIBLE);
                        btnShowDescription.setText("Hide Description");
                    }
                    else {
                        tvDetails.setVisibility(View.GONE);
                        btnShowDescription.setText("Show Description");
                    }
                }
            });
        }

        @Override
        public void onClick(View view) {

            onShopItemListener.onShopItemClick(getAdapterPosition());
        }
    }

    public interface OnShopItemListener{
        void onShopItemClick(int position);
    }
}
