package com.pankajazad.azadlauncher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pankajazad.azadlauncher.databinding.ItemFavoriteAppBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class FavoriteListAdapter extends RecyclerView.Adapter<FavoriteListAdapter.FavoriteViewHolder> {
    interface DragStarter {
        void startDrag(FavoriteViewHolder holder);
    }

    interface ReorderListener {
        void onOrderChanged(List<String> orderedAppIds);
    }

    private final List<AppEntry> apps = new ArrayList<>();
    private final AppActionListener listener;
    private final ReorderListener reorderListener;
    private DragStarter dragStarter = holder -> { };
    private boolean showLabels = true;

    FavoriteListAdapter(AppActionListener listener, ReorderListener reorderListener) {
        this.listener = listener;
        this.reorderListener = reorderListener;
    }

    void submit(List<AppEntry> newApps) {
        apps.clear();
        apps.addAll(newApps);
        notifyDataSetChanged();
    }

    void setShowLabels(boolean showLabels) {
        if (this.showLabels != showLabels) {
            this.showLabels = showLabels;
            notifyDataSetChanged();
        }
    }

    void setDragStarter(DragStarter dragStarter) {
        this.dragStarter = dragStarter;
    }

    boolean move(int fromPosition, int toPosition) {
        if (fromPosition < 0 || toPosition < 0
                || fromPosition >= apps.size() || toPosition >= apps.size()) {
            return false;
        }
        Collections.swap(apps, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        List<String> orderedAppIds = new ArrayList<>();
        for (AppEntry app : apps) {
            orderedAppIds.add(app.getId());
        }
        reorderListener.onOrderChanged(orderedAppIds);
        return true;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFavoriteAppBinding binding = ItemFavoriteAppBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new FavoriteViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        AppEntry app = apps.get(position);
        holder.binding.favoriteIcon.setImageDrawable(app.getIcon());
        holder.binding.favoriteLabel.setText(app.getLabel());
        holder.binding.favoriteLabel.setVisibility(showLabels ? View.VISIBLE : View.GONE);
        holder.binding.getRoot().setContentDescription(app.getLabel());
        holder.binding.getRoot().setOnClickListener(view -> listener.onOpenApp(app));
        holder.binding.getRoot().setOnLongClickListener(view -> listener.onLongPressApp(app));
        holder.binding.dragHandle.setContentDescription(
                holder.binding.getRoot().getContext().getString(R.string.reorder_named_app, app.getLabel()));
        holder.binding.dragHandle.setOnLongClickListener(view -> {
            dragStarter.startDrag(holder);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    static final class FavoriteViewHolder extends RecyclerView.ViewHolder {
        private final ItemFavoriteAppBinding binding;

        FavoriteViewHolder(ItemFavoriteAppBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
