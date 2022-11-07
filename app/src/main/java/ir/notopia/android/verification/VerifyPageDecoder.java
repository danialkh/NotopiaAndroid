package ir.notopia.android.verification;

public class VerifyPageDecoder {


    public static int finder(String main,Character charecter){

        for (int i = 1;i < main.length();i++){

            if(main.charAt(i) == charecter) {
                return i;
            }
        }
        return 1;
    }


    public static long to10(String num) {
        int b = 62;
        String base = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int limit = num.length();
        long res = finder(base,num.charAt(0));

        for(int i = 1;i < limit;i++) {
            res = b * res + finder(base,num.charAt(i));
        }
        return res;
    }

    public static String DecodePageCode(String code){

        long backWard = VerifyPageDecoder.to10(code);
        long a = Long.parseLong("9999999999999");

        backWard = (Long) (a - backWard);
        backWard++;

        String result = String.valueOf(backWard);

        if(result.length() == 12)
            result = "0" + result;

        return result;

    }



}
