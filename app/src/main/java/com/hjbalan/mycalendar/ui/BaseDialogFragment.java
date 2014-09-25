package com.hjbalan.mycalendar.ui;

import com.hjbalan.mycalendar.R;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

public class BaseDialogFragment extends DialogFragment implements OnClickListener {

    public static final String TAG = "baseDialog";

    private Builder mBuilder;

    private TextView tvTitle;

    private TextView tvMessage;

    private Button btnPositive;

    private Button btnNegative;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        if (mBuilder == null) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }
        View dialogView = inflater.inflate(R.layout.dialog_common, container, false);
        tvTitle = (TextView) dialogView.findViewById(R.id.tv_title);
        tvMessage = (TextView) dialogView.findViewById(R.id.tv_message);
        FrameLayout content = (FrameLayout) dialogView.findViewById(R.id.fl_dialog_content);
        btnPositive = (Button) dialogView.findViewById(R.id.btn_positive);
        btnNegative = (Button) dialogView.findViewById(R.id.btn_negative);

        if (TextUtils.isEmpty(mBuilder.mTitle)) {
            tvTitle.setVisibility(View.GONE);
        } else {
            tvTitle.setText(mBuilder.mTitle);
        }

        if (mBuilder.mContentView != null) {
            content.removeAllViews();
            content.addView(mBuilder.mContentView);
            customContentView(mBuilder.mContentView);
        } else if (!TextUtils.isEmpty(mBuilder.mMessage)) {
            tvMessage.setText(mBuilder.mMessage);
        }

        if (TextUtils.isEmpty(mBuilder.mBtnPositiveText)) {
            btnPositive.setVisibility(View.GONE);
        } else {
            btnPositive.setText(mBuilder.mBtnPositiveText);
            btnPositive.setOnClickListener(this);
        }

        if (TextUtils.isEmpty(mBuilder.mBtnNegativeText)) {
            btnNegative.setVisibility(View.GONE);
        } else {
            btnNegative.setText(mBuilder.mBtnNegativeText);
            btnNegative.setOnClickListener(this);
        }

        setCancelable(mBuilder.mCancelable);

        return dialogView;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(mBuilder.mCanceledOnTouchOutside);
        return dialog;
    }

    @Override
    public void onClick(View v) {
        dismiss();
        if (mBuilder.mDialogDismissListener != null) {
            mBuilder.mDialogDismissListener.onDismiss(this);
        }
        switch (v.getId()) {
            case R.id.btn_positive:
                if (mBuilder.mPositiveButtonListener != null) {
                    mBuilder.mPositiveButtonListener
                            .onClick(this, MyDialogInterface.BUTTON_POSITIVE);
                }
                break;

            case R.id.btn_negative:
                if (mBuilder.mNegativeButtonListener != null) {
                    mBuilder.mNegativeButtonListener
                            .onClick(this, MyDialogInterface.BUTTON_NEGATIVE);
                }
                break;

            default:
                break;
        }
    }

    protected void customContentView(View contentView) {

    }

    public void setBuilder(Builder builder) {
        mBuilder = builder;
    }

    public interface MyDialogInterface {

        /**
         * The identifier for the positive button.
         */
        public static final int BUTTON_POSITIVE = -1;

        /**
         * The identifier for the negative button.
         */
        public static final int BUTTON_NEGATIVE = -2;

        /**
         * Interface used to allow the creator of a dialog to run some code when
         * an item on the dialog is clicked.
         *
         * @author alan
         */
        interface OnDialogButtonClickListener {

            /**
             * This method will be invoked when a button in the dialog is
             * clicked.
             *
             * @param dialog The dialog that received the click.
             * @param which  which The button that was clicked
             */
            public void onClick(DialogFragment dialog, int which);
        }

        /**
         * Interface used to allow the creator of a dialog to run some code when
         * the dialog is dismissed.
         *
         * @author alan
         */
        interface OnDialogDismissListener {

            /**
             * This method will be invoked when the dialog is dismissed.
             *
             * @param dialog The dialog that was dismissed will be passed into the
             *               method.
             */
            public void onDismiss(DialogFragment dialog);
        }

        interface OnCancelListener {

            public void onCancel(DialogFragment dialog);
        }

    }

    public static class Builder {

        private Context mContext;

        private String mTitle;

        private String mMessage;

        private String mBtnPositiveText;

        private String mBtnNegativeText;

        private View mContentView;

        private boolean mCancelable;

        private boolean mCanceledOnTouchOutside;

        private MyDialogInterface.OnDialogButtonClickListener mPositiveButtonListener;

        private MyDialogInterface.OnDialogButtonClickListener mNegativeButtonListener;

        private MyDialogInterface.OnDialogDismissListener mDialogDismissListener;

        public Builder(Context context) {
            mContext = context;
        }

        public Context getContext() {
            return mContext;
        }

        public Builder setTitle(int res) {
            return setTitle(mContext.getString(res));
        }

        public Builder setTitle(String title) {
            mTitle = title;
            return this;
        }

        public Builder setMessage(int res) {
            return setMessage(mContext.getString(res));
        }

        public Builder setMessage(String message) {
            mMessage = message;
            return this;
        }

        public Builder setPositiveButton(int textId,
                MyDialogInterface.OnDialogButtonClickListener listener) {
            String text = mContext.getString(textId);
            return setPositiveButton(text, listener);
        }

        public Builder setPositiveButton(String text,
                MyDialogInterface.OnDialogButtonClickListener listener) {
            mBtnPositiveText = text;
            mPositiveButtonListener = listener;
            return this;
        }

        public Builder setNegativeButton(int textId,
                MyDialogInterface.OnDialogButtonClickListener listener) {
            String text = mContext.getString(textId);
            return setNegativeButton(text, listener);
        }

        public Builder setNegativeButton(String text,
                MyDialogInterface.OnDialogButtonClickListener listener) {
            mBtnNegativeText = text;
            mNegativeButtonListener = listener;
            return this;
        }

        public Builder setOnDismissListener(MyDialogInterface.OnDialogDismissListener listener) {
            mDialogDismissListener = listener;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            mCancelable = cancelable;
            return this;
        }

        public Builder setCancelableOutside(boolean cacelableOutside) {
            mCanceledOnTouchOutside = cacelableOutside;
            return this;
        }

        public Builder setContentView(View view) {
            mContentView = view;
            return this;
        }

        public BaseDialogFragment create() {
            BaseDialogFragment newFragment = new BaseDialogFragment();
            newFragment.setBuilder(this);
            return newFragment;
        }
    }

}
