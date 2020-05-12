package app.thecity.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.balysv.materialripple.MaterialRippleLayout;

import java.util.ArrayList;
import java.util.List;

import app.thecity.R;
import app.thecity.data.Constant;
import app.thecity.model.Images;
import app.thecity.utils.Tools;

public class AdapterImageList extends RecyclerView.Adapter<AdapterImageList.ViewHolder> {

    private List<Images> items = new ArrayList<>();
    private OnItemClickListener onItemClickListener;

    private int lastPosition = -1;
    private Context ctx;

    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public ImageView image;
        public MaterialRippleLayout lyt_parent;

        public ViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.image);
            lyt_parent = (MaterialRippleLayout) v.findViewById(R.id.lyt_parent);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public AdapterImageList(Context ctx, List<Images> items) {
        this.ctx = ctx;
        this.items = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // ReString the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final String p = items.get(position).getImageUrl();
        Tools.displayImage(ctx, holder.image, Constant.getURLimgPlace(p));
        holder.lyt_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                // Give some delay to the ripple to finish the effect
                onItemClickListener.onItemClick(v, p, position);
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, String viewModel, int pos);
    }
}