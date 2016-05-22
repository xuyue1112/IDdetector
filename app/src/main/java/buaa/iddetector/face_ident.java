package buaa.iddetector;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.FaceRequest;
import com.iflytek.cloud.RequestListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.facedemo.util.FaceUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link face_ident.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link face_ident#newInstance} factory method to
 * create an instance of this fragment.
 */
public class face_ident extends Fragment
            implements View.OnClickListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;


    private View view;
    // 人脸识别接口相关
    private final int REQUEST_PICTURE_CHOOSE = 1;
    private final int REQUEST_CAMERA_IMAGE = 2;

    private Bitmap mImage = null;
    private byte[] mImageData = null;
    // authid为6-18个字符长度，用于唯一标识用户
    private EditText mAuthid = null;
    private Toast mToast;
    // 进度对话框
    private ProgressDialog mProDialog;
    // 拍照得到的照片文件
    private File mPictureFile;
    // FaceRequest对象，集成了人脸识别的各种功能
    private FaceRequest mFaceRequest;

    public face_ident() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment face_ident.
     */
    // TODO: Rename and change types and number of parameters
    public static face_ident newInstance(String param1, String param2) {
        face_ident fragment = new face_ident();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_face_ident,container,false);
        SpeechUtility.createUtility(getContext(), "appid=" + "5735f117");
        view.findViewById(R.id.online_pick).setOnClickListener(this);
        view.findViewById(R.id.online_reg).setOnClickListener(this);
        view.findViewById(R.id.online_verify).setOnClickListener(this);
        view.findViewById(R.id.online_camera).setOnClickListener(this);
        view.findViewById(R.id.online_detect).setOnClickListener(this);
        view.findViewById(R.id.online_align).setOnClickListener(this);
        mToast = Toast.makeText(getContext(), "", Toast.LENGTH_SHORT);

        mProDialog = new ProgressDialog(getContext());
        mProDialog.setCancelable(true);
        mProDialog.setTitle("请稍后");

        mProDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                // cancel进度框时,取消正在进行的操作
                if (null != mFaceRequest) {
                    mFaceRequest.cancel();
                }
            }
        });

        mFaceRequest = new FaceRequest(getContext());
        Log.i("xytest","create fragment");
        return view;

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }





    //人脸识别接口函数
    private void register(JSONObject obj) throws JSONException {
        int ret = obj.getInt("ret");
        if (ret != 0) {
            showTip("注册失败");
            return;
        }
        if ("success".equals(obj.get("rst"))) {
            showTip("注册成功");
        } else {
            showTip("注册失败");
        }
    }

    private void verify(JSONObject obj) throws JSONException {
        int ret = obj.getInt("ret");
        if (ret != 0) {
            showTip("验证失败");
            return;
        }
        if ("success".equals(obj.get("rst"))) {
            if (obj.getBoolean("verf")) {
                showTip("通过验证，欢迎回来！");
            } else {
                showTip("验证不通过");
            }
        } else {
            showTip("验证失败");
        }
    }

    private void detect(JSONObject obj) throws JSONException {
        int ret = obj.getInt("ret");
        if (ret != 0) {
            showTip("检测失败");
            return;
        }

        if ("success".equals(obj.get("rst"))) {
            JSONArray faceArray = obj.getJSONArray("face");

            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStrokeWidth(Math.max(mImage.getWidth(), mImage.getHeight()) / 100f);

            Bitmap bitmap = Bitmap.createBitmap(mImage.getWidth(),
                    mImage.getHeight(), Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(mImage, new Matrix(), null);
            for (int i = 0; i < faceArray.length(); i++) {
                float x1 = (float) faceArray.getJSONObject(i)
                        .getJSONObject("position").getDouble("left");
                float y1 = (float) faceArray.getJSONObject(i)
                        .getJSONObject("position").getDouble("top");
                float x2 = (float) faceArray.getJSONObject(i)
                        .getJSONObject("position").getDouble("right");
                float y2 = (float) faceArray.getJSONObject(i)
                        .getJSONObject("position").getDouble("bottom");
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawRect(new Rect((int)x1, (int)y1, (int)x2, (int)y2),
                        paint);
            }

            mImage = bitmap;
            ((ImageView) view.findViewById(R.id.online_img)).setImageBitmap(mImage);
        } else {
            showTip("检测失败");
        }
    }

    @SuppressWarnings("rawtypes")
    private void align(JSONObject obj) throws JSONException {
        int ret = obj.getInt("ret");
        if (ret != 0) {
            showTip("聚焦失败");
            return;
        }
        if ("success".equals(obj.get("rst"))) {
            Paint paint = new Paint();
            paint.setColor(Color.BLUE);
            paint.setStrokeWidth(Math.max(mImage.getWidth(), mImage.getHeight()) / 100f);

            Bitmap bitmap = Bitmap.createBitmap(mImage.getWidth(),
                    mImage.getHeight(), Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(mImage, new Matrix(), null);

            JSONArray faceArray = obj.getJSONArray("result");
            for (int i = 0; i < faceArray.length(); i++) {
                JSONObject landmark = faceArray.getJSONObject(i).getJSONObject(
                        "landmark");
                Iterator it = landmark.keys();
                while (it.hasNext()) {
                    String key = (String) it.next();
                    JSONObject postion = landmark.getJSONObject(key);
                    canvas.drawPoint((float) postion.getDouble("x"),
                            (float) postion.getDouble("y"), paint);
                }
            }

            mImage = bitmap;
            ((ImageView) view.findViewById(R.id.online_img)).setImageBitmap(mImage);
        } else {
            showTip("聚焦失败");
        }
    }

    private RequestListener mRequestListener = new RequestListener() {

        @Override
        public void onEvent(int eventType, Bundle params) {
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            if (null != mProDialog) {
                mProDialog.dismiss();
            }

            try {
                String result = new String(buffer, "utf-8");
                Log.d("FaceDemo", result);

                JSONObject object = new JSONObject(result);
                String type = object.optString("sst");
                if ("reg".equals(type)) {
                    register(object);
                } else if ("verify".equals(type)) {
                    verify(object);
                } else if ("detect".equals(type)) {
                    detect(object);
                } else if ("align".equals(type)) {
                    align(object);
                }
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JSONException e) {
                // TODO: handle exception
            }
        }

        @Override
        public void onCompleted(SpeechError error) {
            if (null != mProDialog) {
                mProDialog.dismiss();
            }

            if (error != null) {
                switch (error.getErrorCode()) {
                    case ErrorCode.MSP_ERROR_ALREADY_EXIST:
                        showTip("authid已经被注册，请更换后再试");
                        break;

                    default:
                        showTip(error.getPlainDescription(true));
                        break;
                }
            }
        }
    };

    @Override
    public void onClick(View view) {
        Log.e("xytest","onClick");
        switch (view.getId()) {
            case R.id.online_pick:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(intent, REQUEST_PICTURE_CHOOSE);
                Log.e("xytest","beginPickPicture");
                break;
            case R.id.online_reg:
                mAuthid = ((EditText) this.view.findViewById(R.id.online_authid));
                if(mAuthid == null)
                    Log.e("xytest","user id object is null");

                if (TextUtils.isEmpty(mAuthid.getText().toString())) {
                    showTip("authid不能为空");
                    return;
                }

                if (null != mImageData) {
                    mProDialog.setMessage("注册中...");
                    mProDialog.show();
                    // 设置用户标识，格式为6-18个字符（由字母、数字、下划线组成，不得以数字开头，不能包含空格）。
                    // 当不设置时，云端将使用用户设备的设备ID来标识终端用户。
                    mFaceRequest.setParameter(SpeechConstant.AUTH_ID, mAuthid.getText().toString());
                    mFaceRequest.setParameter(SpeechConstant.WFR_SST, "reg");
                    mFaceRequest.sendRequest(mImageData, mRequestListener);
                } else {
                    showTip("请选择图片后再注册");
                }
                break;
            case R.id.online_verify:
                mAuthid = ((EditText) this.view.findViewById(R.id.online_authid));
                if(mAuthid == null)
                    Log.e("xytest","user id object is null");
                if (TextUtils.isEmpty(mAuthid.getText().toString())) {
                    showTip("authid不能为空");
                    return;
                }

                if (null != mImageData) {
                    mProDialog.setMessage("验证中...");
                    mProDialog.show();
                    // 设置用户标识，格式为6-18个字符（由字母、数字、下划线组成，不得以数字开头，不能包含空格）。
                    // 当不设置时，云端将使用用户设备的设备ID来标识终端用户。
                    mFaceRequest.setParameter(SpeechConstant.AUTH_ID, mAuthid.getText().toString());
                    mFaceRequest.setParameter(SpeechConstant.WFR_SST, "verify");
                    mFaceRequest.sendRequest(mImageData, mRequestListener);
                } else {
                    showTip("请选择图片后再验证");
                }
                break;
            case R.id.online_detect:
                if (null != mImageData) {
                    mProDialog.setMessage("检测中...");
                    mProDialog.show();
                    mFaceRequest.setParameter(SpeechConstant.WFR_SST, "detect");
                    mFaceRequest.sendRequest(mImageData, mRequestListener);
                } else {
                    showTip("请选择图片后再检测");
                }
                break;
            case R.id.online_align:
                if (null != mImageData) {
                    mProDialog.setMessage("聚焦中...");
                    mProDialog.show();
                    mFaceRequest.setParameter(SpeechConstant.WFR_SST, "align");
                    mFaceRequest.sendRequest(mImageData, mRequestListener);
                } else {
                    showTip("请选择图片后再聚集");
                }
                break;
            case R.id.online_camera:
                // 设置相机拍照后照片保存路径
                Log.e("xytest","点击了相机Button!!!");

                File dir = new File(Environment.getExternalStorageDirectory(),"pictures");
                if(dir.exists())
                    dir.mkdirs();
                mPictureFile = new File(dir, System.currentTimeMillis()/1000 + ".jpg");
                if(!mPictureFile.exists()){
                    try {
                        mPictureFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // 启动拍照,并保存到临时文件
                Intent mIntent = new Intent();
                mIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                mIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPictureFile));
                mIntent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
                startActivityForResult(mIntent, REQUEST_CAMERA_IMAGE);
                break;

            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != getActivity().RESULT_OK) {
            return;
        }

        String fileSrc = null;
        if (requestCode == REQUEST_PICTURE_CHOOSE) {
            if ("file".equals(data.getData().getScheme())) {
                Log.e("xytest","PickPicture_old");
                // 有些低版本机型返回的Uri模式为file
                fileSrc = data.getData().getPath();
            } else {
                // Uri模型为content
                Log.e("xytest","PickPicture_new");
                String[] proj = {MediaStore.Images.Media.DATA};
                Cursor cursor = getActivity().getContentResolver().query(data.getData(), proj,
                        null, null, null);
                cursor.moveToFirst();
                int idx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                fileSrc = cursor.getString(idx);
                cursor.close();
            }
            // 跳转到图片裁剪页面
            FaceUtil.cropPicture(this, Uri.fromFile(new File(fileSrc)));
            Log.e("xytest", "cropPicture");
        } else if (requestCode == REQUEST_CAMERA_IMAGE) {
            if (null == mPictureFile) {
                showTip("拍照失败，请重试");
                return;
            }

            fileSrc = mPictureFile.getAbsolutePath();
            updateGallery(fileSrc);
            // 跳转到图片裁剪页面
            FaceUtil.cropPicture(this, Uri.fromFile(new File(fileSrc)));
        } else if (requestCode == com.iflytek.facedemo.util.FaceUtil.REQUEST_CROP_IMAGE) {
            Log.e("xutest","getImage");
            // 获取返回数据
            Bitmap bmp = data.getParcelableExtra("data");
            // 若返回数据不为null，保存至本地，防止裁剪时未能正常保存
            if(null != bmp){
                FaceUtil.saveBitmapToFile(getContext(), bmp);
            }
            // 获取图片保存路径
            fileSrc = FaceUtil.getImagePath(getContext());
            // 获取图片的宽和高
            Options options = new Options();
            options.inJustDecodeBounds = true;
            mImage = BitmapFactory.decodeFile(fileSrc, options);

            // 压缩图片
            options.inSampleSize = Math.max(1, (int) Math.ceil(Math.max(
                    (double) options.outWidth / 1024f,
                    (double) options.outHeight / 1024f)));
            options.inJustDecodeBounds = false;
            mImage = BitmapFactory.decodeFile(fileSrc, options);


            // 若mImageBitmap为空则图片信息不能正常获取
            if(null == mImage) {
                showTip("图片信息无法正常获取！");
                return;
            }

            // 部分手机会对图片做旋转，这里检测旋转角度
            int degree = com.iflytek.facedemo.util.FaceUtil.readPictureDegree(fileSrc);
            if (degree != 0) {
                // 把图片旋转为正的方向
                mImage = com.iflytek.facedemo.util.FaceUtil.rotateImage(degree, mImage);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            //可根据流量及网络状况对图片进行压缩
            mImage.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            mImageData = baos.toByteArray();
            Log.e("xytest","setImage");
            ((ImageView) view.findViewById(R.id.online_img)).setImageBitmap(mImage);
        }

    }

    private void updateGallery(String filename) {
        MediaScannerConnection.scanFile(getContext(), new String[]{filename}, null,
                new MediaScannerConnection.OnScanCompletedListener() {

                    @Override
                    public void onScanCompleted(String path, Uri uri) {

                    }
                });
    }

    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }

}
