package de.qdxposed;

import android.app.Application;
import android.content.Context;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

/**
 * Created by Admin on 24.10.2016.
 */

public class Hook implements IXposedHookLoadPackage {


    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.contains("quizkampen")) {
            return;
        }

        log("**********************************************************");
        log("************** QuizDuell Xposed Starting...***************");
        log("**********************************************************");


        findClasses(lpparam);

        if (MultiDexHelper.getDexCount(lpparam.appInfo) == 1) {
            startHooking(lpparam);
        } else if (MultiDexHelper.getDexCount(lpparam.appInfo) > 1) {
            findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    startHooking(lpparam);
                }
            });
        } else log("NO DEX FILES FOUND, CANNOT DETERMINATE WHERE TO HOOK!");
    }

    private void findClasses(XC_LoadPackage.LoadPackageParam lpparam) {
            log("findClasses");
            Classes.BaseQuestion = XposedHelpers.findClass(Classes.new_names.BaseQuestion, lpparam.classLoader);
    }

    private void startHooking(XC_LoadPackage.LoadPackageParam lpparam) {
        appendAnswer(lpparam);
    }

    private void appendAnswer(XC_LoadPackage.LoadPackageParam lpparam) {
        findAndHookMethod(Classes.BaseQuestion, "getCorrect", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String correct = (String)getObjectField(param.thisObject, "correct");
                correct = (correct != null && correct.charAt(correct.length() - 1) != '✓') ? correct + " ✓" : correct;
                callMethod(param.thisObject, "setCorrect", correct);
            }
        });
    }

    private void log(String data) {
        XposedBridge.log("QuizduellXposed: " + data);
    }
}
