package com.pankajazad.azadlauncher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pankajazad.azadlauncher.databinding.ItemFavoriteAppBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private int iconSizeDp = IconSizeConfiguration.iconSizeDp(IconSizeConfiguration.STANDARD);
    private Set<String> notificationPackages = Collections.emptySet();

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

    void setIconSizeDp(int iconSizeDp) {
        if (this.iconSizeDp != iconSizeDp) {
            this.iconSizeDp = iconSizeDp;
            notifyDataSetChanged();
        }
    }

    void setNotificationPackages(Set<String> notificationPackages) {
        Set<String> updatedPackages = new HashSet<>(notificationPackages);
        if (!this.notificationPackages.equals(updatedPackages)) {
            this.notificationPackages = updatedPackages;
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
        int iconSizePixels = Math.round(
                iconSizeDp * holder.binding.getRoot().getResources().getDisplayMetrics().density);
        ViewGroup.LayoutParams iconLayout = holder.binding.favoriteIcon.getLayoutParams();
        iconLayout.width = iconSizePixels;
        iconLayout.height = iconSizePixels;
        holder.binding.favoriteIcon.setLayoutParams(iconLayout);
        holder.binding.favoriteIcon.setImageDrawable(app.getIcon());
        boolean hasNotification = notificationPackages.contains(app.getPackageName());
        holder.binding.notificationDot.setVisibility(hasNotification ? View.VISIBLE : View.GONE);
        holder.binding.favoriteLabel.setText(app.getLabel());
        holder.binding.favoriteLabel.setVisibility(showLabels ? View.VISIBLE : View.GONE);
        holder.binding.getRoot().setContentDescription(hasNotification
                ? holder.binding.getRoot().getContext().getString(
                        R.string.app_with_notification, app.getLabel())
                : app.getLabel());
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
