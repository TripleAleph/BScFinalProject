import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pawsitivelife.R
import com.example.pawsitivelife.ui.mydogs.ActivityCareItem

class ActivityCareAdapter(
    private val items: List<ActivityCareItem>,
    private val onItemClick: (ActivityCareItem) -> Unit
) : RecyclerView.Adapter<ActivityCareAdapter.ActivityCareViewHolder>() {

    inner class ActivityCareViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.item_IMG_icon)
        val title: TextView = view.findViewById(R.id.item_LBL_text)

        init {
            view.setOnClickListener {
                onItemClick(items[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityCareViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity_care, parent, false)
        return ActivityCareViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityCareViewHolder, position: Int) {
        val item = items[position]
        holder.icon.setImageResource(item.iconResId)
        holder.title.text = item.title
    }

    override fun getItemCount(): Int = items.size
}


