package com.kuya_d.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.kuya_d.Objects.Product;
import com.kuya_d.Objects.ShopItem;
import com.kuya_d.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.dishViewHolder> {

    private final StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    Context context;
    ArrayList<ShopItem> arrDishes;

    public OrderItemAdapter(Context context, ArrayList<ShopItem> arrDishes) {
        this.context = context;
        this.arrDishes = arrDishes;
    }

    @NonNull
    @Override
    public dishViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_menu_item, parent, false);
        return new dishViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull dishViewHolder holder, int position) {
        ShopItem dish = arrDishes.get(position);

        /*if (dish.getProductId() == "-1") {
            Picasso.get().load("https://via.placeholder.com/150?text=No+Image").fit().into(holder.ivProduct);
            holder.tvName.setText("Deleted Item");
            holder.tvPrice.setText("₱--.--");
            holder.tvQuantity.setText("x--");
            holder.tvDetails.setText("Deleted Item");
            holder.tvSubtotal.setText("₱--.--");
            return;
        }*/

        String productId = dish.getId();
        String productName = dish.getProductName();
        // String productDetails = dish.getProductDetails();
        Long thumbnail = dish.getThumbnail();

        holder.tvName.setText(productName);
        // holder.tvDetails.setText(productDetails);
        storageRef.child("products/"+thumbnail).getDownloadUrl()
                .addOnSuccessListener(uri -> Picasso.get().load(uri).resize(120,0).centerCrop().into(holder.ivProduct))
                .addOnFailureListener(e -> Picasso.get().load("https://via.placeholder.com/150?text=No+Image").fit().into(holder.ivProduct));
    }

    @Override
    public int getItemCount() {
        return arrDishes.size();
    }

    public class dishViewHolder extends RecyclerView.ViewHolder {

        RoundedImageView ivProduct;
        TextView tvName, tvDescription;

        public dishViewHolder(@NonNull View itemView) {
            super(itemView);

            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvName = itemView.findViewById(R.id.tvName);
            tvDescription = itemView.findViewById(R.id.tvDetails);
        }
    }
}
