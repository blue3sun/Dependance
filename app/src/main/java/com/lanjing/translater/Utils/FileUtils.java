package com.lanjing.translater.Utils;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.lanjing.translater.MainActivity;
import com.lanjing.translater.R;
import com.lanjing.translater.bean.Constant;
import com.lanjing.translater.bean.TransResult;
import com.lanjing.translater.bean.Translate;
import com.lanjing.translater.bean.TranslateParams;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class FileUtils {
    private static final String TAG = FileUtils.class.getSimpleName();
    private static StringBuilder sStringBuilder = new StringBuilder();
    private static SXSSFWorkbook mSuspectedTallyWorkBook;
    private static final int ROW_RATE = 1;
    private static final int ROW_SRC_TXT = 2;
    private static final int ROW_DET_TXT = 3;
    public static final int EXCEL_MEMORY_LINES = 100;//并不是指100行保存在内存里，而是说100的屏幕尺寸下可见的行数保存在内存中。

    public static Executor executor = Executors.newFixedThreadPool(1);
    private static XSSFWorkbook mXssfWorkbook;

    public static void writeExcelFileData(Workbook wb, String dir, String fileName) {
        try {
            if (wb == null) {
                return;
            }
            if (TextUtils.isEmpty(dir) || TextUtils.isEmpty(fileName)) {
                return;
            }
            File file = new File(dir, fileName);
            writeExcelFileData(wb, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeExcelFileData(Workbook wb, File file) {
        try {
            if (wb == null || file == null) {
                return;
            }
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            //创建文件流
            OutputStream stream = new FileOutputStream(file);
            //写入数据
            wb.write(stream);
            //关闭文件流
            stream.close();
            if (wb instanceof SXSSFWorkbook) {
                ((SXSSFWorkbook) wb).dispose();//清除临时缓存
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 静态方法  解决创建Workbook 创建产生的问题
     *
     * @param inp
     * @return
     * @throws IOException
     * @throws InvalidFormatException
     */
    public static Workbook createworkbook(InputStream inp) throws IOException, InvalidFormatException {
        if (!inp.markSupported()) {
            inp = new PushbackInputStream(inp, 8);
        }
        if (POIFSFileSystem.hasPOIFSHeader(inp)) {
            return new HSSFWorkbook(inp);
        }
        if (POIXMLDocument.hasOOXMLHeader(inp)) {
            return new XSSFWorkbook(OPCPackage.open(inp));
        }
        throw new IllegalArgumentException("你的excel版本目前poi解析不了");
    }

    public static void startTranslateExcelFile(MainActivity mainActivity, String dirName) {
        File dir = new File(dirName);
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return;
        }
        File[] fileList = dir.listFiles();
        int length = fileList.length;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < length; i++) {
                    File file = fileList[i];
                    startTranslateExcelFile(mainActivity, file);
                }
                mainActivity.showProgressbar(false);
            }
        });

    }

    /**
     * 初始化无关记账类的号码，用于过滤短信，减少需要解析的短信数量，节省时间
     *
     * @param mainActivity
     */
    public static void startTranslateExcelFile(MainActivity mainActivity, File file) {
        try {
            InputStream is = new FileInputStream(file);
            //解析excel
//			POIFSFileSystem pSystem=new POIFSFileSystem(is);
            Workbook xssfWb = null;
            try {
                xssfWb = createworkbook(is);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (xssfWb == null) {
                return;
            }
            //获取整个excel
            Sheet sheet = xssfWb.getSheetAt(0);
            if (sheet != null) {
//                int firstNum = sheet.getFirstRowNum();//这里从excel读取的第一行从0开始的即firstNum为0
                int firstNum = 2;
                int lastNum = 4;
//                int lastNum = sheet.getLastRowNum();
                Log.e(TAG, "firstNum:" + firstNum + "    lastNum:" + lastNum);
                String srcTxt = "";
                for (int i = firstNum; i <= lastNum; i++) {
                    Row row = sheet.getRow(i);
                    Cell cell = row.getCell(ROW_SRC_TXT);
                    srcTxt = cell.getStringCellValue();
                    if(TextUtils.isEmpty(srcTxt)){
                        continue;
                    }
                    translate(i,file.getPath(), srcTxt);
                }
                writeExcelFileData(mXssfWorkbook, file);
                mXssfWorkbook=null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void translate(int row,String fileName, String query) {
        RetrofitService retrofitService = RetrofitManager.getRetrofit().create(RetrofitService.class);
        sStringBuilder.setLength(0);
        sStringBuilder.append(Constant.APP_ID).append(query).append(Constant.TRANS_SALT).append(Constant.SECRET_KEY);
        String sign = Md5Utils.stringToMD5(sStringBuilder.toString());
//        TranslateParams translateParams = new TranslateParams();
//        translateParams.setAppid(Constant.APP_ID);
//        translateParams.setFrom(Constant.FROM);
//        translateParams.setTo(Constant.TO);
//        translateParams.setQ(query);
//        translateParams.setSalt(Constant.TRANS_SALT);
//        translateParams.setSign(sign);
//        Observable<Translate> observable = retrofitService.translateByPost(translateParams);

        sStringBuilder.setLength(0);
        Observable<Translate> observable = retrofitService.translateByGet(query, Constant.FROM, Constant.TO, Constant.APP_ID, Constant.TRANS_SALT, sign);

        observable.subscribe(new Subscriber<Translate>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i(TAG, Log.getStackTraceString(e));
                        sStringBuilder.append("null");
                        writeTranslateResult(sStringBuilder.toString(),fileName, row);
                    }

                    @Override
                    public void onNext(Translate translate) {
                        if (translate == null || translate.getTrans_result() == null) {
                            sStringBuilder.append("null");
                            writeTranslateResult(sStringBuilder.toString(),fileName, row);
                            return;
                        }
                        List<TransResult> transList = translate.getTrans_result();
                        TransResult transResult = null;
                        for (int i = 0; i < transList.size(); i++) {
                            transResult = transList.get(i);
                            sStringBuilder.append(transResult == null ? "翻译为空" : transResult.getDst());
                            Log.i(TAG, "Translate Result:" + sStringBuilder.toString());
                            writeTranslateResult(sStringBuilder.toString(),fileName, row);
                        }
                    }
                });
    }


    private static void writeTranslateResult(String translateResult,String filePath, int row) {
        try {
//            long startTime = 0;
//            if (ParamsConfig.mNeedDisplayTime) {
//                startTime = System.currentTimeMillis();
//            }
//
//            File file = null;
//            Workbook wb = null;
//            String sheetName = "";
//            if (isTally) {
//                sheetName = ParamsConfig.TALLY_RESULT_SHEETNAME;
//                file = new File(mParentDirPath, VIVO_TALLY_DISTINCT_RESULT_FILE);
//                if (mTallyWorkBook == null) {
//                    //注:XSSFWorkbook可以追加数据到已经存在的excel表格中，
//                    //但是SXSSFWorkbook附加数据到已经存在的Excel中的话就是不行的，SXSSFWorkbook只能用在新创建Excel中才行。
////                  	XSSFWorkbook xssfWorkbook = new XSSFWorkbook(file);
////						mTallyWorkBook = new SXSSFWorkbook(xssfWorkbook,EXCEL_MEMORY_LINES);
//                    //使用SXSSFWorkbook可以提升excel文件写入的性能
//                    mTallyWorkBook = new SXSSFWorkbook(ParamsConfig.EXCEL_MEMORY_LINES);
//                }
//                wb = mTallyWorkBook;
//            } else {
//                sheetName = ParamsConfig.SUSPECTED_TALLY_RESULT_SHEETNAME;
//                file = new File(mParentDirPath, VIVO_SUSPECTED_TALLY_DISTINCT_RESULT_FILE);
//                if (mSuspectedTallyWorkBook == null) {
////                    XSSFWorkbook xssfWorkbook = new XSSFWorkbook(file);
////					  mSuspectedTallyWorkBook = new SXSSFWorkbook(xssfWorkbook,EXCEL_MEMORY_LINES);
//                    //使用SXSSFWorkbook可以提神excel文件写入的性能
//                    mSuspectedTallyWorkBook = new SXSSFWorkbook(ParamsConfig.EXCEL_MEMORY_LINES);
//                }
//                wb = mSuspectedTallyWorkBook;
//            }
//			Workbook wb = WorkbookFactory.create(file);

//            if (mSuspectedTallyWorkBook == null) {
////                    XSSFWorkbook xssfWorkbook = new XSSFWorkbook(file);
////					  mSuspectedTallyWorkBook = new SXSSFWorkbook(xssfWorkbook,EXCEL_MEMORY_LINES);
//                //使用SXSSFWorkbook可以提神excel文件写入的性能
////                mSuspectedTallyWorkBook = new SXSSFWorkbook(EXCEL_MEMORY_LINES);
//
//                XSSFWorkbook xssfWorkbook = new XSSFWorkbook(filePath);
////                mSuspectedTallyWorkBook = new SXSSFWorkbook(xssfWorkbook,EXCEL_MEMORY_LINES);
//            }
//            SXSSFWorkbook wb = mSuspectedTallyWorkBook;

            if (mXssfWorkbook == null) {
//                    XSSFWorkbook xssfWorkbook = new XSSFWorkbook(file);
//					  mSuspectedTallyWorkBook = new SXSSFWorkbook(xssfWorkbook,EXCEL_MEMORY_LINES);
                //使用SXSSFWorkbook可以提神excel文件写入的性能
//                mSuspectedTallyWorkBook = new SXSSFWorkbook(EXCEL_MEMORY_LINES);

               mXssfWorkbook = new XSSFWorkbook(filePath);
            }
            Workbook wb = mXssfWorkbook;

            Sheet sheet = wb.getSheetAt(0);

            Cell cell = sheet.getRow(row).createCell(ROW_DET_TXT);
            cell.setCellValue(translateResult);

            //使用SXSSFWorkbook不能在这里就调用写入文件的方法，
            // 否则会出现异常：java.lang.IllegalArgumentException: Attempting to write a row[0] in the range [0,1] that is already written to disk.
//			FileUtils.writeExcelFileData(wb,file);

        } catch (Exception e) {
            Log.e(TAG, "write translate result error", e);
        }
    }
}
