package api_classes.interfaces;

/**
 * Created by dmitry on 06.09.17.
 */

public interface ReportedLocationUploaderInterface {
    void onSuccessUploadReportedLocation();
    void onErrorUploadReportedLocation(String errorText);
}
