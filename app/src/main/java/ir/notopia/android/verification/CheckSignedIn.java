package ir.notopia.android.verification;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class CheckSignedIn {

    public CheckSignedIn(Context context){

        SharedPreferences login = context.getSharedPreferences("SignedIn_PR", Context.MODE_PRIVATE);
        String userNumber = login.getString("USER_NUMBER_PR", null);
        String userName = login.getString("USER_NAME_PR", null);
        String userFamily = login.getString("USER_FAMILY_PR", null);

        if(userNumber == null && userName == null && userFamily == null){
            Intent verificationIntent = new Intent(context, EnterNumberActivity.class);
            verificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            ((Activity)context).startActivity(verificationIntent);
            ((Activity)context).finish();

        }
        else{

//            AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
//            builder1.setMessage(loginCode);
//            builder1.setCancelable(true);
//
//            builder1.setPositiveButton(
//                    "باشه",
//                    (dialog, id) -> dialog.cancel());
//
//
//            AlertDialog alert11 = builder1.create();
//            alert11.show();

        }
    }
}
