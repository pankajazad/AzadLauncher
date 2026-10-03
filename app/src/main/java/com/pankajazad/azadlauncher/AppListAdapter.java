package com.pankajazad.azadlauncher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pankajazad.azadlauncher.databinding.ItemAppBinding;

import java.util.ArrayList;
import java.util.List;

final class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.AppViewHolder> {
    private final List<AppEntry> apps = new ArrayList<>();
    private final AppActionListener listener;
    private boolean showLabels = true;
    private int iconSizeDp = IconSizeConfiguration.iconSizeDp(IconSizeConfiguration.STANDARD);

    AppListAdapter(AppActionListener listener) {
        this.listener = listener;
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

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAppBinding binding = ItemAppBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AppViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        AppEntry app = apps.get(position);
        int iconSizePixels = Math.round(
                iconSizeDp * holder.binding.getRoot().getResources().getDisplayMetrics().density);
        ViewGroup.LayoutParams iconLayout = holder.binding.appIcon.getLayoutParams();
        iconLayout.width = iconSizePixels;
        iconLayout.height = iconSizePixels;
        holder.binding.appIcon.setLayoutParams(iconLayout);
        holder.binding.appIcon.setImageDrawable(app.getIcon());
        holder.binding.appLabel.setText(app.getLabel());
        holder.binding.appLabel.setVisibility(showLabels ? View.VISIBLE : View.GONE);
        holder.binding.getRoot().setContentDescription(app.getLabel());
        holder.binding.getRoot().setOnClickListener(view -> listener.onOpenApp(app));
        holder.binding.getRoot().setOnLongClickListener(view -> listener.onLongPressApp(app));
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    static final class AppViewHolder extends RecyclerView.ViewHolder {
        private final ItemAppBinding binding;

        AppViewHolder(ItemAppBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
