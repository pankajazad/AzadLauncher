package com.pankajazad.azadlauncher;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pankajazad.azadlauncher.databinding.ItemFolderAppBinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class FolderAppListAdapter extends RecyclerView.Adapter<FolderAppListAdapter.ViewHolder> {
    interface Listener {
        void onIncludedChanged(AppEntry app, boolean included);
    }

    private final List<AppEntry> apps = new ArrayList<>();
    private final Set<String> includedAppIds = new HashSet<>();
    private final Listener listener;

    FolderAppListAdapter(Listener listener) {
        this.listener = listener;
    }

    void submit(List<AppEntry> newApps, Set<String> includedIds) {
        apps.clear();
        apps.addAll(newApps);
        includedAppIds.clear();
        includedAppIds.addAll(includedIds);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFolderAppBinding binding = ItemFolderAppBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppEntry app = apps.get(position);
        holder.binding.folderAppIcon.setImageDrawable(app.getIcon());
        holder.binding.folderAppLabel.setText(app.getLabel());
        holder.binding.folderAppCheck.setOnCheckedChangeListener(null);
        holder.binding.folderAppCheck.setChecked(includedAppIds.contains(app.getId()));
        holder.binding.folderAppCheck.setOnCheckedChangeListener((button, included) -> {
            if (included) {
                includedAppIds.add(app.getId());
            } else {
                includedAppIds.remove(app.getId());
            }
            listener.onIncludedChanged(app, included);
        });
        holder.binding.getRoot().setOnClickListener(
                view -> holder.binding.folderAppCheck.toggle());
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemFolderAppBinding binding;

        ViewHolder(ItemFolderAppBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
