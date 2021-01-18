package com.example.pickhere;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.listeners.TableDataClickListener;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

public class Parking extends AppCompatActivity {

    static String[][] spaceProbes = {
            {"22/06/2019", "10"},
            {"22/06/2019", "10"},
            {"22/06/2019", "10"}

    };

    static String[] spaceProbeHeaders = {"Date", "POINTS EARNED"};

/*
    @RequiresApi(api = Build.VERSION_CODES.P)
*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking);

        final TableView<String[]> tableView = (TableView<String[]>) findViewById(R.id.tableview);

//Set prop
        tableView.setHeaderBackgroundColor( Color.parseColor("aqua"));
        tableView.setHeaderAdapter(new SimpleTableHeaderAdapter(this, spaceProbeHeaders));
        tableView.setColumnCount(2);
        tableView.setBackgroundColor(Color.parseColor("aqua"));

        tableView.setDataAdapter(new SimpleTableDataAdapter(Parking.this, spaceProbes));

        tableView.addDataClickListener(new TableDataClickListener<String[]>() {
            @Override
            public void onDataClicked(int rowIndex, String[] clickedData) {
                Toast.makeText(Parking.this, ((String[])clickedData)[1], Toast.LENGTH_SHORT).show();
            }
        });
    }
}
