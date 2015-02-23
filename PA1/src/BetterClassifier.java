import java.io.IOException;

/**
 * Created by Sofonias on 2/23/2015.
 */
public class BetterClassifier {

    public void main(String[] args) throws IOException {

        TrainingModel t = new TrainingModel(TrainingModel.DIRECTORY);

        new DataProcessor(false, true, false);

    }

}
