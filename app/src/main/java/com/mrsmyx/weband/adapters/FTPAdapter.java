package com.mrsmyx.weband.adapters;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.mrsmyx.weband.Global;
import com.mrsmyx.weband.R;

import java.io.File;
import java.io.IOException;

import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
import it.sauronsoftware.ftp4j.FTPListParseException;

/**
 * Created by CJ on 9/27/2015.
 */
public abstract class FTPAdapter extends RecyclerView.Adapter<FTPAdapter.FTPHolder> {

    private FTPFile[] files;
    public FTPClient ftpClient;
    private final int HEADER = 0, NORMAL = 1;

    public FTPAdapter() {
        ftpClient = new FTPClient();
    }

    int length = 0;
    Handler handler = new Handler();

    public void connect(final String ip) throws FTPException, IOException, FTPIllegalReplyException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ftpClient.connect(ip);
                    ftpClient.login("anonymous", "ftp4j");
                    length = ftpClient.list().length + 1;
                    files = ftpClient.list();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyDataSetChanged();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (FTPIllegalReplyException e) {
                    e.printStackTrace();
                } catch (FTPException e) {
                    e.printStackTrace();
                } catch (FTPAbortedException e) {
                    e.printStackTrace();
                } catch (FTPListParseException e) {
                    e.printStackTrace();
                } catch (FTPDataTransferException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void changeDir(final String dir) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ftpClient.changeDirectory(dir);
                    length = ftpClient.list().length + 1;
                    files = ftpClient.list();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyDataSetChanged();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    if(!ftpClient.isConnected()){
                        try {
                            connect(Global.IP);
                        } catch (FTPException e1) {
                            e1.printStackTrace();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        } catch (FTPIllegalReplyException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    public void goUp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ftpClient.changeDirectoryUp();
                    length = ftpClient.list().length + 1;
                    files = ftpClient.list();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyDataSetChanged();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (FTPIllegalReplyException e) {
                    e.printStackTrace();
                } catch (FTPException e) {
                    e.printStackTrace();
                } catch (FTPAbortedException e) {
                    e.printStackTrace();
                } catch (FTPListParseException e) {
                    e.printStackTrace();
                } catch (FTPDataTransferException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    @Override
    public FTPHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == HEADER)
            return new FTPHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.ftp_card, parent, false));
        else if (viewType == NORMAL)
            return new FTPHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.ftp_card, parent, false));
        else return null;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == HEADER) return HEADER;
        else return NORMAL;
    }

    @Override
    public void onBindViewHolder(FTPHolder holder, int position) {
        if (getItemViewType(position) == HEADER) {
            holder.title.setText("..");
            holder.sub.setText("Go up");
            holder.imageView.setImageResource(R.mipmap.ic_folder_black_48dp);
        } else if (getItemViewType(position) == NORMAL) {
            holder.title.setText(files[position - 1].getName());
            holder.sub.setText(files[position - 1].getModifiedDate().toLocaleString());
            if (files[position - 1].getType() == FTPFile.TYPE_DIRECTORY) {
                holder.imageView.setImageResource(R.mipmap.ic_folder_black_48dp);
            } else if (files[position - 1].getType() == FTPFile.TYPE_FILE) {
                holder.imageView.setImageResource(R.mipmap.ic_insert_drive_file_black_48dp);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (!ftpClient.isConnected()) {
            Log.e("NOT CONNECTED", "NOT CONNECTED");
            return 0;
        }
        return length;
    }

    public FTPFile[] getFiles() {
        return files;
    }

    public FTPFile getFileAtPosition(int index) {
        return files[index];
    }

    public abstract void OnFTPClicked(View view, int position);

    public abstract boolean OnFTPLongClicked(View view, int position);

    public abstract void OnFTPFolderClicked(View view, int position);

    public void upload(final File file, final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ftpClient.upload(file, new FTPDataTransferListener() {
                        @Override
                        public void started() {
                            Toast.makeText(context, "Upload Started!", Toast.LENGTH_SHORT).show();

                        }

                        @Override
                        public void transferred(int i) {

                        }

                        @Override
                        public void completed() {
                            Toast.makeText(context, "Upload completed!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void aborted() {
                            Toast.makeText(context,"Upload Aborted", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void failed() {
                            Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (FTPIllegalReplyException e) {
                    e.printStackTrace();
                } catch (FTPException e) {
                    e.printStackTrace();
                } catch (FTPDataTransferException e) {
                    e.printStackTrace();
                } catch (FTPAbortedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public class FTPHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        AppCompatTextView title, sub;
        ImageView imageView;

        public FTPHolder(View itemView) {
            super(itemView);
            title = (AppCompatTextView) itemView.findViewById(R.id.ftp_title);
            sub = (AppCompatTextView) itemView.findViewById(R.id.ftp_sub);
            imageView = (ImageView) itemView.findViewById(R.id.ftp_img);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }


        @Override
        public void onClick(View v) {
            if (getLayoutPosition() > 0 && files[getLayoutPosition() - 1].getType() == FTPFile.TYPE_DIRECTORY) {
                OnFTPFolderClicked(v, getLayoutPosition());
                return;
            } else if (getLayoutPosition() == 0) {
                goUp();
                return;
            }
            OnFTPClicked(v, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            return OnFTPLongClicked(v, getLayoutPosition());
        }
    }
}
