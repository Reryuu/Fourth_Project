package com.example.nreaderapp.adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nreaderapp.MyApplication;
import com.example.nreaderapp.activities.PdfDetailActivity;
import com.example.nreaderapp.activities.admin.PdfEditActivity;
import com.example.nreaderapp.databinding.RowPdfAdminBinding;
import com.example.nreaderapp.filters.FilterPdfAdmin;
import com.example.nreaderapp.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

public class AdapterPdfAdmin extends RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin> implements Filterable {
    private static final long MAX_BYTES_PDF = 50000000;
    private static final String TAG = "PDF_ADAPTER_TAG";
    private Context context;
    public ArrayList<ModelPdf> pdfArrayList, filterList;
    private RowPdfAdminBinding binding;
    private FilterPdfAdmin filter;
    private ProgressDialog progressDialog;

    private void moreOptionDialog(ModelPdf modelPdf, HolderPdfAdmin holder) {
        String bookId = modelPdf.getId();
        String bookUrl = modelPdf.getUrl();
        String bookTitle = modelPdf.getTitle();

        String[] options = {"Edit", "Delete"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose option").setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0){
                    Intent intent = new Intent(context, PdfEditActivity.class);
                    intent.putExtra("bookId", bookId);
                    context.startActivity(intent);
                }else if (i == 1){
                    MyApplication.deleteBook(context, "" + bookId, "" + bookUrl, "" + bookTitle);
                }
            }
        }).show();
    }

    public AdapterPdfAdmin(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;

        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public HolderPdfAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderPdfAdmin(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterPdfAdmin.HolderPdfAdmin holder, int position) {
        ModelPdf modelPdf = pdfArrayList.get(position);
        String title = modelPdf.getTitle();
        String description = modelPdf.getDescription();
        long timestamp = modelPdf.getTimestamp();
        String pdfUrl = modelPdf.getUrl();
        String pdfId = modelPdf.getId();
        String categoryId = modelPdf.getCategoryId();
        String formattedDate = MyApplication.formatTimestamp(timestamp);

        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(formattedDate);

        MyApplication.loadCategory("" + categoryId, holder.categoryTv);
        MyApplication.loadPdfFromUrlSinglePage("" + pdfUrl, "" + title, holder.pdfView, holder.progressBar, null);
        MyApplication.loadPdfSize("" + pdfUrl, "" + title, holder.sizeTv);

        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moreOptionDialog(modelPdf, holder);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId", pdfId);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterPdfAdmin(filterList, this);
        }
        return filter;
    }

    class HolderPdfAdmin extends RecyclerView.ViewHolder{
        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTv, descriptionTv, categoryTv, sizeTv, dateTv;
        ImageButton moreBtn;


        public HolderPdfAdmin(@NonNull View itemView) {
            super(itemView);
            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            categoryTv = binding.categoryTv;
            sizeTv = binding.sizeTv;
            dateTv = binding.dateTv;
            moreBtn = binding.moreBtn;
        }
    }
}
