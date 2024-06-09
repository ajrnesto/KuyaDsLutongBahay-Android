package com.kuya_d.Adapters;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.kuya_d.Objects.CartItem;
import com.kuya_d.Objects.ShopItem;
import com.kuya_d.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.cartItemViewHolder> {

    private final FirebaseFirestore DB = FirebaseFirestore.getInstance();
    private final FirebaseAuth AUTH = FirebaseAuth.getInstance();
    private final StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    Context context;
    private Activity activity;
    ArrayList<CartItem> arrCartItem;
    ArrayList<ShopItem> arrProducts;
    private OnCartItemListener mOnCartItemListener;
    private OnQuantityChanged mOnQuantityChanged;
    private OnItemDeleted mOnItemDeleted;

    public CartItemAdapter(Context context, Activity activity, ArrayList<CartItem> arrCartItem, ArrayList<ShopItem> arrProducts, OnCartItemListener onCartItemListener, OnQuantityChanged onQuantityChanged, OnItemDeleted onItemDeleted) {
        this.context = context;
        this.activity = activity;
        this.arrCartItem = arrCartItem;
        this.arrProducts = arrProducts;
        this.mOnCartItemListener = onCartItemListener;
        this.mOnQuantityChanged = onQuantityChanged;
        this.mOnItemDeleted = onItemDeleted;
    }

    @NonNull
    @Override
    public cartItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_cart_item, parent, false);
        return new cartItemViewHolder(view, mOnCartItemListener, mOnQuantityChanged, mOnItemDeleted);
    }

    @Override
    public void onBindViewHolder(@NonNull cartItemViewHolder holder, int position) {
        CartItem cartItem = arrCartItem.get(position);
        ShopItem product = arrProducts.get(position);

        int quantity = cartItem.getQuantity();
        String productName = product.getProductName();
        Long thumbnail = product.getThumbnail();

        DecimalFormat df = new DecimalFormat("0.00");
        holder.tvName.setText(productName);
        holder.etQuantity.setText(""+quantity);

        storageRef.child("products/"+thumbnail).getDownloadUrl()
                .addOnSuccessListener(uri -> Picasso.get().load(uri).resize(120,100).centerInside().into(holder.ivProduct))
                .addOnFailureListener(e -> Picasso.get().load("https://via.placeholder.com/150?text=No+Image").fit().into(holder.ivProduct));
    }

    @Override
    public int getItemCount() {
        return arrCartItem.size();
    }

    public class cartItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        OnCartItemListener onCartItemListener;
        OnQuantityChanged onQuantityChanged;
        OnItemDeleted onItemDeleted;
        AppCompatImageView ivProduct;
        TextView tvName;
        MaterialButton btnDecrement, btnIncrement, btnRemove;
        TextInputEditText etQuantity;

        public cartItemViewHolder(@NonNull View itemView, OnCartItemListener onCartItemListener, OnQuantityChanged onQuantityChanged, OnItemDeleted onItemDeleted) {
            super(itemView);

            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvName = itemView.findViewById(R.id.tvName);
            btnDecrement = itemView.findViewById(R.id.btnDecrement);
            btnIncrement = itemView.findViewById(R.id.btnIncrement);
            btnRemove = itemView.findViewById(R.id.btnRemove);
            etQuantity = itemView.findViewById(R.id.etQuantity);

            this.onCartItemListener = onCartItemListener;
            this.onQuantityChanged = onQuantityChanged;
            itemView.setOnClickListener(this);

            btnRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MaterialAlertDialogBuilder dialogDelete = new MaterialAlertDialogBuilder(itemView.getContext());
                    dialogDelete.setTitle("Remove Item");
                    dialogDelete.setMessage("Are you sure you want to remove this item from you cart?");
                    dialogDelete.setNeutralButton("Cancel", (dialogInterface, i) -> {

                    });
                    dialogDelete.setNegativeButton("Delete Item", (dialogInterface, i) -> {
                        DB.collection("carts").document(Objects.requireNonNull(AUTH.getCurrentUser()).getUid())
                                .collection("items").document(arrProducts.get(getAdapterPosition()).getId())
                                .delete()
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(view.getContext(), "Removed item from your cart.", Toast.LENGTH_SHORT).show();

                                    Log.d("DEBUG", "ARRAY OF CART ITEMS: "+arrCartItem);
                                    /*arrCartItem.remove(getAdapterPosition());
                                    arrProducts.remove(getAdapterPosition());*/

                                    onItemDeleted.onItemDeleted(getAdapterPosition());
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(view.getContext(), "Failed to remove item from your cart. Error: "+e, Toast.LENGTH_SHORT).show();
                                });

                    });
                    dialogDelete.show();
                }
            });
        }

        @Override
        public void onClick(View view) {
            onCartItemListener.onCartItemClick(getAdapterPosition());
        }
    }

    public interface OnCartItemListener{
        void onCartItemClick(int position);
    }

    public interface OnQuantityChanged {
        void onQuantityChanged(int position, int quantity);
    }

    public interface OnItemDeleted {
        void onItemDeleted(int position);
    }
}
