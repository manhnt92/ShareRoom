package com.manhnt.shareroom;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import com.afollestad.materialdialogs.MaterialDialog;
import com.manhnt.adapter.CommentsAdapter;
import com.manhnt.config.Config;
import com.manhnt.config.DialogManager;
import com.manhnt.config.RequestAPI;
import com.manhnt.config.WidgetManager;
import com.manhnt.object.Account;
import com.manhnt.object.Comments;
import com.manhnt.object.MyRooms;
import com.manhnt.object.Room;
import com.manhnt.widget.BlurBehind;
import com.manhnt.widget.EmojiconWidget;
import com.rengwuxian.materialedittext.MaterialEditText;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SearchRoomCommentsActivity extends Activity implements View.OnClickListener, CommentsAdapter.OnLongItemClickListener {

    private Account mAccount;
    private Room currentRoom;
    private ArrayList<Comments> list_comment;
    private CommentsAdapter adapter;
    private String send_comment;
    private SwipeRefreshLayout refreshLayout;
    private boolean isRefresh;
    private RecyclerView listView;
    private MaterialEditText edit_comment;
    private int currentPage = 1;
    private int totalPage;
    private WidgetManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comments_activity);
        BlurBehind.getInstance().setBackground(this);
        getExtraBundle();
        getWidget();
    }

    private void getExtraBundle(){
        if(getIntent().getExtras() != null) {
            boolean isFavoriteRooms = getIntent().getExtras().getBoolean(Config.BUNDLE_IS_FAVORITE_ROOM);
            if(isFavoriteRooms){
                MyRooms myFavoriteRooms = (MyRooms) getIntent().getExtras().getSerializable(Config.BUNDLE_MY_ROOMS);
                assert myFavoriteRooms != null;
                currentRoom = myFavoriteRooms.getList_room().get(myFavoriteRooms.getPosition());
                mAccount = (Account) getIntent().getExtras().getSerializable(Config.BUNDLE_ACCOUNT);
            }else {
                int position_room_in_list_room_after = getIntent().getExtras().getInt(Config.BUNDLE_POSITION_ROOM_IN_LIST_ROOM_AFTER);
                currentRoom = Config.LIST_ROOM_AFTER.get(position_room_in_list_room_after);
                mAccount = (Account) getIntent().getExtras().getSerializable(Config.BUNDLE_ACCOUNT);
            }
        }
    }

    private void getWidget(){
        manager = WidgetManager.getInstance(this);
        manager.TextView(R.id.txt_comments, true);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(currentPage <= totalPage){
                    currentPage++;
                    isRefresh = true;
                    RequestAPI.getInstance().context(SearchRoomCommentsActivity.this).method(RequestAPI.GET)
                        .url(Config.URL_SEARCH_ROOM_COMMENTS + "?room_id=" + currentRoom.getId() + "&page=" + currentPage)
                        .isParams(false).isAuthorization(true).message(getString(R.string.waiting)).isShowToast(true)
                        .isShowDialog(false).execute(GetCommentsListener);
                } else {
                    refreshLayout.setRefreshing(false);
                }
            }
        });
        listView = (RecyclerView) findViewById(R.id.commentsListView);
        LinearLayoutManager llManager = new LinearLayoutManager(this);
        llManager.setOrientation(LinearLayoutManager.VERTICAL);
        listView.setLayoutManager(llManager);
        list_comment = new ArrayList<>();
        adapter = new CommentsAdapter(this, list_comment);
        listView.setAdapter(adapter);
        adapter.setOnLongItemClickListener(this);
        final EmojiconWidget widget = new EmojiconWidget(this, EmojiconWidget.RELATIVE_LAYOUT, R.id.root_view,
            R.id.btn_emojicon, R.id.edt_comment, R.id.btn_send_comment, true);
        widget.setSendListener(new EmojiconWidget.SendListener() {
            @Override
            public void onBtnSendClick(View view) {
                MaterialEditText edt_comment = widget.getEditText();
                String comment = edt_comment.getText().toString();
                send_comment = Config.convertStringToHexString(comment);
                edt_comment.setText("");
                if(!TextUtils.isEmpty(send_comment)){
                    RequestAPI.getInstance().context(SearchRoomCommentsActivity.this).url(Config.URL_COMMENT)
                        .method(RequestAPI.POST).isParams(true).isAuthorization(true).message(getString(R.string.waiting))
                        .isShowToast(true).isShowDialog(true).execute(SendCommentListener);
                }
            }
        });
        RequestAPI.getInstance().context(SearchRoomCommentsActivity.this).method(RequestAPI.GET)
            .url(Config.URL_SEARCH_ROOM_COMMENTS + "?room_id=" + currentRoom.getId() + "&page=" + currentPage)
            .isParams(false).isAuthorization(true).message(getString(R.string.waiting)).isShowToast(false)
            .isShowDialog(false).execute(GetCommentsListener);
    }

    @Override
    public void onClick(View view) {}

    @SuppressWarnings("deprecation")
    @Override
    public void onLongClick(final int position) {
        final Comments comments = list_comment.get(position);
        final boolean isMyComment;
        String[] items;
        if(comments.getUser_id() == mAccount.getId()){
            items = getResources().getStringArray(R.array.my_comment_control);
            isMyComment = true;
        } else {
            items = getResources().getStringArray(R.array.comment_control);
            isMyComment = false;
        }
        DialogManager.getInstance().ListOneChoiceDialog(this, R.string.comments, items, -1, false, true,
        new DialogManager.ListOneChoiceDialogListener() {
            @Override
            public void onChoice(MaterialDialog dialog, int index) {
                if(isMyComment) {
                    if (index == Config.MY_COMMENT_EDIT) {
                        showDialogEditComment(comments, position);
                    } else if (index == Config.MY_COMMENT_DELETE) {
                        showDialogDeleteComment(comments, position);
                    } else if (index == Config.MY_COMMENT_COPY){
                        Log.d("COMMENT DIALOG", "My comment copy");
                    }
                } else {
                    if(index == Config.COMMENT_COPY){
                        Log.d("COMMENT DIALOG", "comment copy");
                    }
                }
            }
        }).show();
    }

    private RequestAPI.RequestAPIListener GetCommentsListener = new RequestAPI.RequestAPIListener() {
        @Override
        public JSONObject onRequest() throws JSONException {
            return null;
        }

        @Override
        public String onAuthorization() {
            return mAccount.getApi_key();
        }

        @Override
        public void onResult(String contentMessage) throws JSONException {
            if(!contentMessage.equalsIgnoreCase("")) {
                JSONObject jObj = new JSONObject(contentMessage);
                currentPage = jObj.optInt(Config.CURRENT_PAGE);
                totalPage = jObj.optInt(Config.TOTAL_PAGE);
                JSONArray jArray = jObj.optJSONArray(Config.CONTENT_MESSAGE);
                if (isRefresh) {
                    ArrayList<Comments> new_comments = new ArrayList<>();
                    Config.convertJsonToComments(jArray, new_comments);
                    list_comment.addAll(0, new_comments);
                    refreshLayout.setRefreshing(false);
                    isRefresh = false;
                    adapter.notifyDataSetChanged();
                } else {
                    Config.convertJsonToComments(jArray, list_comment);
                    adapter.notifyDataSetChanged();
                    if(list_comment.size() > 0) {
                        listView.smoothScrollToPosition(list_comment.size() - 1);
                    }
                }
            } else {
                refreshLayout.setRefreshing(false);
            }
        }

        @Override
        public void onError(Exception e) {
            isRefresh = false;
            refreshLayout.setRefreshing(false);
        }
    };

    private RequestAPI.RequestAPIListener SendCommentListener = new RequestAPI.RequestAPIListener() {
        @Override
        public JSONObject onRequest() throws JSONException {
            JSONObject jObj = new JSONObject();
            jObj.put(Config.COMMENT, send_comment);
            jObj.put(Config.ROOM_ID, currentRoom.getId());
            return jObj;
        }

        @Override
        public String onAuthorization() {
            return mAccount.getApi_key();
        }

        @Override
        public void onResult(String contentMessage) throws JSONException {
            Comments c = new Comments(Integer.parseInt(contentMessage), mAccount.getId(), send_comment,
                mAccount.getAvatar(), mAccount.getFirst_name() + " " + mAccount.getLast_name(),
                Config.convertCurrentTimeCommentsDateTime(System.currentTimeMillis()));
            list_comment.add(c);
            adapter.notifyDataSetChanged();
            listView.smoothScrollToPosition(list_comment.size() - 1);
        }

        @Override
        public void onError(Exception e) {}
    };

    private void showDialogEditComment(final Comments comment, final int index){
        final String cmt = Config.convertHexStringToString(comment.getComment());
        DialogManager.getInstance().CustomViewDialog(this, R.string.edit_comment, R.layout.edit_comment_dialog,
        true, new DialogManager.CustomViewListener() {
            @Override
            public void onAttachCustomView(View view) {
                edit_comment = manager.MaterialEditText(view, R.id.edt_comment, true);
            }

            @Override
            public void onOK(MaterialDialog dialog) {
                if(!edit_comment.getText().toString().equalsIgnoreCase(cmt)){
                    final String new_cmt = Config.convertStringToHexString(edit_comment.getText().toString());
                    RequestAPI.getInstance().context(SearchRoomCommentsActivity.this).method(RequestAPI.PUT)
                    .url(Config.URL_COMMENT + "/" + comment.getId()).isShowDialog(true).isParams(true)
                    .isAuthorization(true).isShowToast(true).message(getString(R.string.waiting))
                    .execute(new RequestAPI.RequestAPIListener() {
                        @Override
                        public JSONObject onRequest() throws JSONException {
                            JSONObject jObj = new JSONObject();
                            jObj.put(Config.COMMENT, new_cmt);
                            return jObj;
                        }

                        @Override
                        public String onAuthorization() {
                            return mAccount.getApi_key();
                        }

                        @Override
                        public void onResult(String contentMessage) throws JSONException {
                            comment.setComment(new_cmt);
                            list_comment.set(index, comment);
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onError(Exception e) {}
                    });
                }
                dialog.dismiss();
            }

            @Override
            public void onCancel(MaterialDialog dialog) {
                dialog.dismiss();
            }
        }).show();
    }

    private void showDialogDeleteComment(final Comments comment, final int index){
        DialogManager.getInstance().YesNoDialog(this, R.string.delete_comment, R.string.question_delete_comment,
        R.string.OK, R.string.back, new DialogManager.YesNoDialogListener() {
            @Override
            public void onYes(MaterialDialog dialog) {
                RequestAPI.getInstance().context(SearchRoomCommentsActivity.this).method(RequestAPI.DELETE)
                    .url(Config.URL_COMMENT + "/" + comment.getId()).isAuthorization(true).isParams(false)
                    .isShowDialog(true).isShowToast(true).message(getString(R.string.waiting))
                    .execute(new RequestAPI.RequestAPIListener() {
                    @Override
                    public JSONObject onRequest() throws JSONException {
                        return null;
                    }

                    @Override
                    public String onAuthorization() {
                        return mAccount.getApi_key();
                    }

                    @Override
                    public void onResult(String contentMessage) throws JSONException {
                        list_comment.remove(index);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Exception e) {}
                });
            }

            @Override
            public void onNo(MaterialDialog dialog) {
                dialog.dismiss();
            }
        }, true).show();
    }

}
