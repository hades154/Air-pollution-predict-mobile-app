package com.example.airquality;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.airquality.model.Location;
import com.example.airquality.viewmodel.LocationDAO;

import java.util.List;


public class Notifications {
    private Context context;
    public Notifications(Context context) {
        this.context=context;
    }

    public void setUpNotification() {
        AppDatabase appDatabase = AppDatabase.Instance(context.getApplicationContext());
        LocationDAO locationDAO = appDatabase.locationDAO();
        Location location;

        Location favouriteLocation = locationDAO.getFavouriteLocation();
        if (favouriteLocation == null) {
            List<Location> markedLocation = locationDAO.getListHasMark();
            if (markedLocation.size() == 0) {
                location = locationDAO.getByID(1);
            } else {
                location = markedLocation.get(0);
            }
        }
        else location = favouriteLocation;

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            // When sdk version is larger than26
            String id = "channel_1";
            String description = "143";
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.avatar_green);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(id, description, importance);
            manager.createNotificationChannel(channel);
            android.app.Notification notification = new android.app.Notification.Builder(context.getApplicationContext(), id)
                    .setCategory(android.app.Notification.CATEGORY_MESSAGE)
                    .setSmallIcon(R.drawable.app_logo)
                    .setLargeIcon(getCroppedBitmap(location.getRated()))
                    .setContentTitle("AQI :"+(int)location.getAqi()+" - "+location.getRated())
                    .setContentText(getRecommended(location.getRated()))
                    .setSubText(location.getStationName())
                    .setAutoCancel(false).setOngoing(true).build();
            manager.notify(1, notification);
        } else {
            // When sdk version is less than26
            android.app.Notification notification = new NotificationCompat.Builder(context.getApplicationContext())
                    .setContentTitle("This is content title")
                    .setContentText("This is content text").setSmallIcon(R.mipmap.ic_launcher)
                    .build();
            manager.notify(1, notification);
        }
    }

    public Bitmap getCroppedBitmap(String rate) {
        Bitmap bitmap= BitmapFactory.decodeResource(context.getResources(),R.drawable.avatar_green);;
        switch (rate) {
            case "T???t": // Xanh l??
                bitmap= BitmapFactory.decodeResource(context.getResources(),R.drawable.avatar_green);
                break;
            case "Trung b??nh": // V??ng
                bitmap= BitmapFactory.decodeResource(context.getResources(),R.drawable.avatar_yellow);
                break;
            case "K??m": // Cam
                bitmap= BitmapFactory.decodeResource(context.getResources(),R.drawable.avatar_orange);
                break;
            case "X???u": // ?????
                bitmap= BitmapFactory.decodeResource(context.getResources(),R.drawable.avatar_red);
                break;
            case "R???t x???u": // T??m
                bitmap= BitmapFactory.decodeResource(context.getResources(),R.drawable.avatar_purple);
                break;
            case "Nguy h???i": // N??u
                bitmap= BitmapFactory.decodeResource(context.getResources(),R.drawable.avatar_brown);
                break;
        }

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }
    public String getRecommended(String rate){
        String recommended="";
        switch (rate) {
            case "T???t": // Xanh l??
                recommended="Kh??ng ???nh h?????ng t???i s???c kh???e";
                break;
            case "Trung b??nh": // V??ng
                recommended="Nh??m nh???y c???m n??n h???n ch??? th???i gian ??? b??n ngo??i";
                break;
            case "K??m": // Cam
                recommended="Nh??m nh???y c???m h???n ch??? th???i gian ??? b??n ngo??i";
                break;
            case "X???u": // ?????
                recommended="Nh??m nh???y c???m tr??nh ra ngo??i. Nh???ng ng?????i kh??c h???n ch??? ra ngo??i";
                break;
            case "R???t x???u": // T??m
                recommended="Nh??m nh???y c???m tr??nh ra ngo??i. Nh???ng ng?????i kh??c h???n ch??? ra ngo??i";
                break;
            case "Nguy h???i": // N??u
                recommended="M???i ng?????i n??n ??? nh??";
                break;
        }
        return recommended;
    }
}
