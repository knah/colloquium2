package ru.ifmo.md.colloquium2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    CandidateAdapter candidateAdapter;
    VotingAdapter votingAdapter;
    ResultsAdapter resultsAdapter;

    private enum WorkingMode {
        MODE_PREVOTE,
        MODE_VOTE,
        MODE_RESULTS
    }

    WorkingMode mode;
    KeyValueStore store;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        candidateAdapter = new CandidateAdapter();
        votingAdapter = new VotingAdapter();
        resultsAdapter = new ResultsAdapter();

        store = new KeyValueStore(this);

        SQLiteDatabase db = store.getWritableDatabase();
        String vd = store.getData(db, "voting");
        String modeStored = store.getData(db, "mode");

        if(vd != null && modeStored != null) {
            VotingData.clear();
            VotingData.readData(vd);
            mode = WorkingMode.values()[Integer.parseInt(modeStored)];
        } else {
            mode = WorkingMode.MODE_PREVOTE;
        }

        changeMode(mode);
    }

    private void changeMode(WorkingMode newMode) {

        mode = newMode;

        ListView list = ((ListView) findViewById(R.id.listView));
        switch(mode) {
            case MODE_PREVOTE:
                list.setAdapter(candidateAdapter);
                list.setOnItemClickListener(null);
                list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                        PopupMenu popup = new PopupMenu(MainActivity.this, view);
                        MenuInflater inflater = popup.getMenuInflater();
                        inflater.inflate(R.menu.menu_edit, popup.getMenu());
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                int id = item.getItemId();
                                if(id == R.id.action_delete) {
                                    VotingData.removeCandidate(VotingData.getCandidateName(position));
                                    onDatasetChange();
                                    return true;
                                } else if(id == R.id.action_edit) {
                                    AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                                    final EditText input = new EditText(MainActivity.this);
                                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                                    input.setText(VotingData.getCandidateName(position));
                                    alert.setView(input);
                                    alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Editable value = input.getText();
                                            String name = value.toString();
                                            if(!VotingData.renameCandidate(VotingData.getCandidateName(position), name)) {
                                                Toast.makeText(MainActivity.this, R.string.bad_caniddate_name, Toast.LENGTH_SHORT).show();
                                            }
                                            onDatasetChange();
                                        }
                                    });
                                    alert.setNegativeButton(R.string.cancel, null);
                                    alert.show();
                                    return true;
                                }
                                return false;
                            }
                        });
                        popup.show();
                        return true;
                    }
                });
                break;
            case MODE_VOTE:
                list.setAdapter(votingAdapter);
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        VotingData.addVote(VotingData.getCandidateName(position));
                        storeDatabase();
                    }
                });
                list.setOnItemLongClickListener(null);
                break;
            case MODE_RESULTS:
                VotingData.sortByVotes();
                list.setAdapter(resultsAdapter);
                onDatasetChange();
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        changeMode(WorkingMode.MODE_PREVOTE);
                    }
                });
                list.setOnItemLongClickListener(null);
                break;
        }

        onDatasetChange();
    }

    private void onDatasetChange() {
        candidateAdapter.notifyDataSetChanged();
        votingAdapter.notifyDataSetChanged();
        resultsAdapter.notifyDataSetChanged();

        storeDatabase();
    }

    private void storeDatabase() {
        SQLiteDatabase db = store.getWritableDatabase();
        store.setData(db, "voting", VotingData.writeData());
        store.setData(db, "mode", mode.ordinal() + "");
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.clear();
        switch(mode) {
            case MODE_PREVOTE:
                getMenuInflater().inflate(R.menu.menu_main, menu);
                return true;
            case MODE_VOTE:
                getMenuInflater().inflate(R.menu.menu_voting, menu);
                return true;
            case MODE_RESULTS:
                return false; // no menu here
        }

        throw new RuntimeException("Reached unreachable statement");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            alert.setView(input);
            alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Editable value = input.getText();
                    String name = value.toString();
                    if(!VotingData.addCandidate(name)) {
                        Toast.makeText(MainActivity.this, R.string.bad_caniddate_name, Toast.LENGTH_SHORT).show();
                    }
                    onDatasetChange();
                }
            });
            alert.setNegativeButton(R.string.cancel, null);
            alert.show();
            return true;
        } else if(id == R.id.action_start_vote) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to start voting?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    VotingData.clearVotes();
                    changeMode(WorkingMode.MODE_VOTE);
                }
            }).setNegativeButton("No", null).show();
            return true;
        } else if(id == R.id.action_clear_candidates) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to remove ALL candidates?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    VotingData.clear();
                    onDatasetChange();
                }
            }).setNegativeButton("No", null).show();
        } else if(id == R.id.action_finish) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to finish voting?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    changeMode(WorkingMode.MODE_RESULTS);
                }
            }).setNegativeButton("No", null).show();
        }

        return super.onOptionsItemSelected(item);
    }

    private static class CandidateAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return VotingData.getCount();
        }

        @Override
        public String getItem(int position) {
            return VotingData.getCandidateName(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView w = (TextView) convertView;
            if(w == null) {
                w = new TextView(parent.getContext());
                w.setTextSize(40.0f);
                w.setClickable(false);
            }

            w.setText(getItem(position));

            return w;
        }
    }

    private static class VotingAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return VotingData.getCount();
        }

        @Override
        public String getItem(int position) {
            return VotingData.getCandidateName(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView w = (TextView) convertView;
            if(w == null) {
                w = new TextView(parent.getContext());
                w.setTextSize(40.0f);
                w.setClickable(false);
            }

            w.setText(getItem(position));

            return w;
        }
    }

    private static class ResultsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return VotingData.getCount() + 1;
        }

        @Override
        public String getItem(int position) {
            return VotingData.getCandidateName(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView w = (TextView) convertView;
            if(w == null) {
                w = new TextView(parent.getContext());
                w.setTextSize(40.0f);
            }

            if(position == 0) {
                w.setBackgroundColor(0xFFFF0000);
            } else {
                w.setBackgroundColor(0xFFFFFFFF);
            }

            if(position == VotingData.getCount()) {
                w.setText("Done, close results");
                w.setClickable(false);
            } else {
                w.setText(getItem(position) + " " + VotingData.getPercentage(getItem(position)));
                w.setClickable(true);
            }

            return w;
        }
    }
}
