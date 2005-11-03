package nonregressiontest.component.nfpriority;

public interface PrecisionController {
    public static final String PRECISION_CONTROLLER_NAME = "precision-controller";

    public void increasePrecision();

    public void decreasePrecision();
}
