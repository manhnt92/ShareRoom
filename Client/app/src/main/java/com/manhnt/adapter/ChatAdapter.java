package com.manhnt.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.manhnt.config.Config;
import com.manhnt.object.Account;
import com.manhnt.object.ChatMessage;
import com.manhnt.shareroom.R;
import java.util.ArrayList;

import github.ankushsachdeva.emojicon.EmojiconTextView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatHolder>{

    private ArrayList<ChatMessage> list_chats;
    private Account mAccount;
    private Context mContext;

    public ChatAdapter(Context context, ArrayList<ChatMessage> objects, Account account){
        this.mContext = context;
        this.list_chats = objects;
        this.mAccount = account;
    }

    @Override
    public ChatHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, parent, false);
        return new ChatHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(ChatHolder holder, int position) {
        if(list_chats.get(position).getTo_id() == mAccount.getId()){
            holder.ll_user1.setVisibility(View.VISIBLE);
            holder.ll_user2.setVisibility(View.GONE);
            holder.user1_message.setText(Config.convertHexStringToString(list_chats.get(position).getMessage()));
            holder.user1_time_send.setText(list_chats.get(position).getCreated());
        } else {
            holder.ll_user1.setVisibility(View.GONE);
            holder.ll_user2.setVisibility(View.VISIBLE);
            holder.user2_message.setText(Config.convertHexStringToString(list_chats.get(position).getMessage()));
            holder.user2_time_send.setText(list_chats.get(position).getCreated());
            switch (list_chats.get(position).getStatus()){
                case 1:
                    holder.user2_message_status.setImageResource(R.drawable.ic_message_send_to_server);
                    break;
                case 2:
                    holder.user2_message_status.setImageResource(R.drawable.ic_message_send_to_user);
                    break;
                case 3:
                    holder.user2_message_status.setImageResource(R.drawable.ic_message_read);
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return list_chats.size();
    }

    public class ChatHolder extends RecyclerView.ViewHolder {

        LinearLayout ll_user1, ll_user2;
        EmojiconTextView user1_message;
        TextView user1_time_send;
        EmojiconTextView user2_message;
        TextView user2_time_send;
        ImageView user2_message_status;

        public ChatHolder(View itemView) {
            super(itemView);
            ll_user1 = (LinearLayout) itemView.findViewById(R.id.ll_user1);
            ll_user2 = (LinearLayout) itemView.findViewById(R.id.ll_user2);
            user1_message = (EmojiconTextView) itemView.findViewById(R.id.user1_message);
            user1_message.setTypeface(Config.getTypeface(mContext.getAssets()));
            user1_time_send = (TextView) itemView.findViewById(R.id.user1_time_send);
            user1_time_send.setTypeface(Config.getTypeface(mContext.getAssets()));
            user2_message = (EmojiconTextView) itemView.findViewById(R.id.user2_message);
            user2_message.setTypeface(Config.getTypeface(mContext.getAssets()));
            user2_time_send = (TextView) itemView.findViewById(R.id.user2_time_send);
            user2_time_send.setTypeface(Config.getTypeface(mContext.getAssets()));
            user2_message_status = (ImageView) itemView.findViewById(R.id.user2_message_status);
        }
    }

}
