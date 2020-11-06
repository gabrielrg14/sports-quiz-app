package edu.fatec.profg.quizapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SportsQuizFragment extends Fragment {

    private static final int NUMBER_OF_SPORTS_TO_FINISH_QUIZ = 2;

    private List<String> allSportsNamesList;
    private List<String> quizSportsNamesSelectedList;
    private String correctSportName;

    private int totalNumberOfTries;
    private int numberOfRightTries;
    private int numberOfsportsOptionsRows;
    private SecureRandom randomNumber;

    // Elementos da interface
    private LinearLayout[] sportsOptionsRows;
    private ImageView sportImage;
    private TextView progressIndicatorTxt;
    private TextView answerTxt;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.sports_quiz_fragment, container, false);

        // Instância variáveis da classe
        allSportsNamesList = new ArrayList<>();
        quizSportsNamesSelectedList = new ArrayList<>();

        numberOfsportsOptionsRows = 3; // Este valor deveria ser configurável
        randomNumber = new SecureRandom();

        // Obtém referência para elementos da GUI
        sportImage = view.findViewById(R.id.sport_img);
        progressIndicatorTxt = view.findViewById(R.id.progress_indicator_txt);
        answerTxt = view.findViewById(R.id.answer_txt);
        sportsOptionsRows = new LinearLayout[3];
        sportsOptionsRows[0] = (LinearLayout) view.findViewById(R.id.first_row_linear);
        sportsOptionsRows[1] = (LinearLayout) view.findViewById(R.id.second_row_linear);
        sportsOptionsRows[2] = (LinearLayout) view.findViewById(R.id.third_row_linear);

        // Ajusta texto de progresso inicial
        progressIndicatorTxt.setText(getString(R.string.quiz_progress_text,
                1, NUMBER_OF_SPORTS_TO_FINISH_QUIZ));

        // Atribui tratador de eventos aos botões de opção
        for(LinearLayout row : sportsOptionsRows) {
            for(int column = 0; column < row.getChildCount(); column++) {
                Button btnOption = (Button) row.getChildAt(column);

                btnOption.setOnClickListener(btnOptionListener);
            }
        }

        resetSportsQuiz();
        return view;
    }

    private View.OnClickListener btnOptionListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            Button btnOption = (Button) view;
            String optionValue = btnOption.getText().toString();
            String answerValue = getExactSportName(correctSportName);

            totalNumberOfTries++;

            // Verifica se o Jogador acertou
            if(answerValue.equals(optionValue)) {
                // O jogador Acertou
                numberOfRightTries++;
                answerTxt.setText(answerValue + "! CORRETO");

                disableAllOptionButtons();

                // Verifica condição de encerramento
                if(numberOfRightTries == NUMBER_OF_SPORTS_TO_FINISH_QUIZ) {
                    // Encerrarento da quiz
                    DialogFragment resultsDialog = new ResultDialog(SportsQuizFragment.this,
                            totalNumberOfTries, numberOfRightTries);
                    resultsDialog.setCancelable(false);
                    resultsDialog.show(getFragmentManager(), "SportsQuizResults");
                } else {
                    // Continuar a quiz
                    showNextSportImg();
                }
            } else {
                // O jogador Errou
                answerTxt.setText("ERRADO!!");
                btnOption.setEnabled(false);
                btnOption.setBackgroundResource(R.drawable.buttonred);
            }
        }
    };

    private void showNextSportImg() {
        this.correctSportName = quizSportsNamesSelectedList.remove(0);
        answerTxt.setText("");
        progressIndicatorTxt.setText(getString(R.string.quiz_progress_text,
                numberOfRightTries + 1,
                NUMBER_OF_SPORTS_TO_FINISH_QUIZ));

        // Pega a imagem dos "assets"
        AssetManager assets = getActivity().getAssets();
        try {
            InputStream stream = assets.open("sport_imgs/" + correctSportName);
            Drawable drawableImg = Drawable.createFromStream(stream, correctSportName);
            sportImage.setImageDrawable(drawableImg);
        } catch(IOException e) {
            Log.e("SPORTS_QUIZ_ERRO", e.getMessage());
        }

        Collections.shuffle(allSportsNamesList);
        int correctSportIndex = allSportsNamesList.indexOf(getExactSportName(correctSportName));
        allSportsNamesList.remove(correctSportIndex);

        for(int row = 0; row < numberOfsportsOptionsRows; row++) {
            LinearLayout llRow = sportsOptionsRows[row];
            for(int column = 0; column < llRow.getChildCount(); column++) {
                Button btnOption = (Button) llRow.getChildAt(column);
                btnOption.setEnabled(true);
                btnOption.setBackgroundResource(R.drawable.buttonwhite);
                String sportImgName = allSportsNamesList.get((row*2) + column);
                btnOption.setText(sportImgName);
            }
        }

        allSportsNamesList.add(getExactSportName(correctSportName));

        // Define botão com opção correta
        int row = randomNumber.nextInt(numberOfsportsOptionsRows);
        int column = randomNumber.nextInt(2);
        LinearLayout rndRow = sportsOptionsRows[row];
        ((Button) rndRow.getChildAt(column)).setText(getExactSportName(correctSportName));
    }

    private String getExactSportName(String sportName) {
        return sportName.substring(0, sportName.indexOf('.')).replace('_', ' ');
    }

    private void disableAllOptionButtons() {
        // Atribui tratador de eventos aos botões de opção
        for(LinearLayout row : sportsOptionsRows) {
            for(int column = 0; column < row.getChildCount(); column++) {
                Button btnOption = (Button) row.getChildAt(column);
                btnOption.setEnabled(false);
            }
        }
    }

    private void resetSportsQuiz() {
        AssetManager assets = getActivity().getAssets();
        allSportsNamesList.clear();
        String[] sportImagesPaths = null;

        try {
            sportImagesPaths = assets.list("sport_imgs");
            for(String sportImgName : sportImagesPaths) {
                allSportsNamesList.add(getExactSportName(sportImgName));
            }
        } catch(IOException e) {
            Log.e("SPORTS_QUIZ_ERRO", e.getMessage());
        }

        // Montar a Quiz
        numberOfRightTries = 0;
        totalNumberOfTries = 0;
        quizSportsNamesSelectedList.clear();

        int counter = 1;
        while(counter <= NUMBER_OF_SPORTS_TO_FINISH_QUIZ) {
            int randIndex = randomNumber.nextInt(sportImagesPaths.length);
            String chosenSportName = sportImagesPaths[randIndex];
            if(!quizSportsNamesSelectedList.contains(chosenSportName)) {
                quizSportsNamesSelectedList.add(chosenSportName);
                counter++;
            }
        }

        showNextSportImg();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public static class ResultDialog extends DialogFragment {
        private int totalNumberOfTries;
        private int numberOfRightTries;
        private SportsQuizFragment context;

        public ResultDialog(SportsQuizFragment context, int totalNumberOfTries, int numberOfRightTries) {
            this.context = context;
            this.totalNumberOfTries = totalNumberOfTries;
            this.numberOfRightTries = numberOfRightTries;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder diaBuilder = new AlertDialog.Builder(getActivity());
            diaBuilder.setMessage(getString(R.string.results_text,
                    totalNumberOfTries, numberOfRightTries, ((float)(100 * numberOfRightTries /  totalNumberOfTries))));
            diaBuilder.setPositiveButton(R.string.reset_quiz, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    context.resetSportsQuiz();
                }
            });
            return diaBuilder.create();
        }
    }
}