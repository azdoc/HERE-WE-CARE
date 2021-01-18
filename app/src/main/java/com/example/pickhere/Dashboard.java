package com.example.pickhere;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class Dashboard extends AppCompatActivity {

    GridLayout mainGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_dashboard );

        mainGrid = (GridLayout)findViewById(R.id.mainGrid);
        setSingleEvent(mainGrid);

    }

    public void setSingleEvent(GridLayout mainGrid) {
        for(int i = 0; i<mainGrid.getChildCount(); i++)
        {
            CardView cardView = (CardView)mainGrid.getChildAt(i);
            final int finalI = i;
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    if(finalI == 0)
                    {
                        Intent intent = new Intent(Dashboard.this, Reporting.class);
                        startActivity(intent);
                    }
                    else if(finalI == 1)
                    {
                        Intent intent = new Intent(Dashboard.this, Parking.class);
                        startActivity(intent);
                    }
                    else if(finalI == 2)
                    {
                        Intent intent = new Intent(Dashboard.this, Freeride.class);
                        startActivity(intent);
                    }
                    else if(finalI == 3)
                    {
                        Intent intent = new Intent(Dashboard.this, Leaderboard.class);
                        startActivity(intent);
                    }

                    else
                    {
                        Toast.makeText(Dashboard.this, "Please set activity for this card item", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
