package com.example.pickhere;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.listeners.TableDataClickListener;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

public class Freeride extends AppCompatActivity {

    static String[][] spaceProbes = {
            {"30", "30", "60", "6 free ride"}
    };

    static String[] spaceProbeHeaders = {"SOCIAL POINT", "PARKING POINT", "TOTAL POINTS", "RIDE GAINED"};

/*
    @RequiresApi(api = Build.VERSION_CODES.P)
*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_freeride);

        final TableView<String[]> tableView = (TableView<String[]>) findViewById(R.id.tableview);

//Set prop
        tableView.setHeaderBackgroundColor( Color.parseColor("aqua"));
        tableView.setHeaderAdapter(new SimpleTableHeaderAdapter(this, spaceProbeHeaders));
        tableView.setColumnCount(4);
        tableView.setBackgroundColor(Color.parseColor("aqua"));

        tableView.setDataAdapter(new SimpleTableDataAdapter(Freeride.this, spaceProbes));

        tableView.addDataClickListener(new TableDataClickListener<String[]>() {
            @Override
            public void onDataClicked(int rowIndex, String[] clickedData) {
                Toast.makeText(Freeride.this, ((String[])clickedData)[1], Toast.LENGTH_SHORT).show();
            }
        });
    }
}
