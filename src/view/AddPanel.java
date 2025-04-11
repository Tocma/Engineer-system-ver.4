package view;

import model.EngineerDTO;
import model.EngineerBuilder;
import controller.MainController;
import util.LogHandler;
import util.LogHandler.LogType;
import util.MessageEnum;
import util.Validator;
import util.ValidatorEnum;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * エンジニア情報の新規登録画面を提供するパネルクラス
 * 各種入力フィールドとバリデーション機能を実装し、フィールド毎のエラー表示に対応
 *
 * <p>
 * このクラスは、エンジニア人材管理システムにおいて新規エンジニア情報を
 * 登録するためのユーザーインターフェースを提供します。AbstractEngineerPanelを
 * 継承し、エンジニア情報の入力フォームと登録処理を実装しています。
 * </p>
 *
 * <p>
 * バージョン4.1.0での主な改善点：
 * <ul>
 * <li>フィールド毎のエラー表示機能の実装 - エラーメッセージが各フィールドの下に表示</li>
 * <li>複数のバリデーションエラーを同時に表示する機能</li>
 * <li>エラー発生時のフォーカス制御の改善</li>
 * <li>バリデーションメソッドの再構築と明確化</li>
 * </ul>
 * </p>
 *
 * <p>
 * 主な機能：
 * <ul>
 * <li>エンジニア情報の入力フォーム表示</li>
 * <li>必須項目と任意項目の区別</li>
 * <li>入力データの検証</li>
 * <li>登録ボタンによるデータ保存</li>
 * <li>フォームのクリア機能</li>
 * <li>一覧画面への戻り機能</li>
 * </ul>
 * </p>
 *
 * <p>
 * このパネルは以下の入力項目を含みます：
 * <ul>
 * <li>氏名（必須）</li>
 * <li>社員ID（必須）</li>
 * <li>フリガナ（必須）</li>
 * <li>生年月日（必須）</li>
 * <li>入社年月</li>
 * <li>エンジニア歴（必須）</li>
 * <li>扱える言語（必須）</li>
 * <li>経歴</li>
 * <li>研修の受講歴</li>
 * <li>技術力、受講態度、コミュニケーション能力、リーダーシップ</li>
 * <li>備考</li>
 * </ul>
 * </p>
 *
 * <p>
 * 登録処理の流れ：
 * <ol>
 * <li>ユーザーが入力フォームに必要情報を入力</li>
 * <li>登録ボタンをクリック</li>
 * <li>入力値の検証実行</li>
 * <li>検証エラーがあれば対応するフィールドにエラーメッセージを表示</li>
 * <li>検証成功時はEngineerDTOオブジェクトを構築</li>
 * <li>MainControllerを通じてデータ保存処理を実行</li>
 * <li>保存成功時は確認ダイアログを表示し、フォームをクリア</li>
 * </ol>
 * </p>
 *
 * @author Nakano
 * @version 4.1.0
 * @since 2025-04-11
 */
public class AddPanel extends AbstractEngineerPanel {

    /** メインコントローラー参照 */
    private MainController mainController;

    /** 登録ボタン */
    private JButton addButton;

    /** 戻るボタン */
    private JButton backButton;

    /** プログレスインジケーター */
    private JLabel progressLabel;

    /** 処理中フラグ */
    private boolean processing;

    /** 完了処理の成功フラグ */
    private boolean handleSaveCompleteSuccess = false;

    /** 年のコンボボックス（生年月日用） */
    private JComboBox<String> birthYearComboBox;

    /** 月のコンボボックス（生年月日用） */
    private JComboBox<String> birthMonthComboBox;

    /** 日のコンボボックス（生年月日用） */
    private JComboBox<String> birthDayComboBox;

    /** 年のコンボボックス（入社年月用） */
    private JComboBox<String> joinYearComboBox;

    /** 月のコンボボックス（入社年月用） */
    private JComboBox<String> joinMonthComboBox;

    /** エンジニア歴コンボボックス */
    private JComboBox<String> careerComboBox;

    /** 技術力コンボボックス */
    private JComboBox<String> technicalSkillComboBox;

    /** 受講態度コンボボックス */
    private JComboBox<String> learningAttitudeComboBox;

    /** コミュニケーション能力コンボボックス */
    private JComboBox<String> communicationSkillComboBox;

    /** リーダーシップコンボボックス */
    private JComboBox<String> leadershipComboBox;

    /** 扱える言語リスト */
    private List<JCheckBox> languageCheckBoxes;

    /** 氏名フィールド */
    private JTextField nameField;

    /** 社員IDフィールド */
    private JTextField idField;

    /** フリガナフィールド */
    private JTextField nameKanaField;

    /** 経歴テキストエリア */
    private JTextArea careerHistoryArea;

    /** 研修の受講歴テキストエリア */
    private JTextArea trainingHistoryArea;

    /** 備考テキストエリア */
    private JTextArea noteArea;

    /** ダイアログマネージャー */
    private DialogManager dialogManager;

    /** フィールド名と表示名のマッピング */
    private Map<String, String> fieldDisplayNames;

    /**
     * コンストラクタ
     * パネルの初期設定とコンポーネントの初期化
     */
    public AddPanel() {
        super();
        this.processing = false;
        this.languageCheckBoxes = new ArrayList<>();
        this.dialogManager = DialogManager.getInstance();
        this.fieldDisplayNames = new HashMap<>();
        initializeFieldDisplayNames();
        LogHandler.getInstance().log(Level.INFO, LogType.UI, "AddPanelを作成しました");
    }

    /**
     * フィールド名と表示名のマッピングを初期化
     * バリデーションエラー表示時に使用
     */
    private void initializeFieldDisplayNames() {
        fieldDisplayNames.put("nameField", "氏名");
        fieldDisplayNames.put("idField", "社員ID");
        fieldDisplayNames.put("nameKanaField", "フリガナ");
        fieldDisplayNames.put("birthDate", "生年月日");
        fieldDisplayNames.put("joinDate", "入社年月");
        fieldDisplayNames.put("careerComboBox", "エンジニア歴");
        fieldDisplayNames.put("languages", "扱える言語");
        fieldDisplayNames.put("careerHistoryArea", "経歴");
        fieldDisplayNames.put("trainingHistoryArea", "研修の受講歴");
        fieldDisplayNames.put("technicalSkillComboBox", "技術力");
        fieldDisplayNames.put("learningAttitudeComboBox", "受講態度");
        fieldDisplayNames.put("communicationSkillComboBox", "コミュニケーション能力");
        fieldDisplayNames.put("leadershipComboBox", "リーダーシップ");
        fieldDisplayNames.put("noteArea", "備考");
    }

    /**
     * メインコントローラーを設定
     * コントローラーを通じてイベント処理や画面遷移を行う
     *
     * @param mainController メインコントローラー
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * パネルの初期化
     * UIコンポーネントの生成と配置を行う
     */
    @Override
    public void initialize() {
        // 親クラスの初期化
        super.initialize();

        try {
            // フォームコンポーネントの作成
            createFormComponents();

            // ボタン領域の作成
            createButtonArea();

            // 入力検証の設定
            setupValidation();

            LogHandler.getInstance().log(Level.INFO, LogType.UI, "AddPanelの初期化が完了しました");
        } catch (Exception e) {
            LogHandler.getInstance().logError(LogType.UI, "AddPanelの初期化中にエラーが発生しました", e);
        }
    }

    /**
     * フォームコンポーネントの作成と配置
     * 入力フィールドとラベルをパネルに配置
     */
    private void createFormComponents() {
        // 左側のフォームパネル（基本情報）
        JPanel leftFormPanel = new JPanel();
        leftFormPanel.setLayout(new BoxLayout(leftFormPanel, BoxLayout.Y_AXIS));
        leftFormPanel.setBackground(Color.WHITE);

        // 右側のフォームパネル（スキル情報）
        JPanel rightFormPanel = new JPanel();
        rightFormPanel.setLayout(new BoxLayout(rightFormPanel, BoxLayout.Y_AXIS));
        rightFormPanel.setBackground(Color.WHITE);

        // 1. 基本情報セクション（左側）
        createBasicInfoSection(leftFormPanel);

        // 2. 言語スキルセクション（左側）
        createLanguageSection(leftFormPanel);

        // 3. 経歴セクション（左側）
        createCareerHistorySection(leftFormPanel);

        // 4. スキルセクション（右側）
        createSkillSection(rightFormPanel);

        // 5. 研修の受講歴セクション（右側）
        createTrainingSection(rightFormPanel);

        // 6. 備考セクション（右側）
        createNoteSection(rightFormPanel);

        // 左右のパネルを水平に配置
        JPanel formContainer = new JPanel(new GridLayout(1, 2, 20, 0));
        formContainer.setBackground(Color.WHITE);
        formContainer.add(leftFormPanel);
        formContainer.add(rightFormPanel);

        // メインパネルに追加
        panel.add(formContainer);
    }

    /**
     * 基本情報セクションの作成
     * 氏名、社員ID、生年月日などの基本情報入力フィールドを配置
     *
     * @param container 配置先のコンテナ
     */
    private void createBasicInfoSection(JPanel container) {

        // 氏名フィールド（必須）
        JLabel nameLabel = createFieldLabel("氏名", true);
        nameField = new JTextField(20);
        registerComponent("nameField", nameField);
        container.add(createFormRow(nameLabel, nameField, "nameField"));
        container.add(createVerticalSpacer(10));

        // 社員IDフィールド（必須）
        JLabel idLabel = createFieldLabel("社員ID", true);
        idField = new JTextField(20);
        registerComponent("idField", idField);
        container.add(createFormRow(idLabel, idField, "idField"));
        container.add(createVerticalSpacer(10));

        // フリガナフィールド（必須）
        JLabel nameKanaLabel = createFieldLabel("フリガナ", true);
        nameKanaField = new JTextField(20);
        registerComponent("nameKanaField", nameKanaField);
        container.add(createFormRow(nameKanaLabel, nameKanaField, "nameKanaField"));
        container.add(createVerticalSpacer(10));

        // 生年月日（必須）- 年・月・日のコンボボックス
        JLabel birthDateLabel = createFieldLabel("生年月日", true);
        JPanel birthDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        birthDatePanel.setBackground(Color.WHITE);

        // 年コンボボックス
        birthYearComboBox = new JComboBox<>(getYearOptions(1950, LocalDate.now().getYear()));
        birthYearComboBox.setPreferredSize(new Dimension(80, 25));
        registerComponent("birthYearComboBox", birthYearComboBox);
        birthDatePanel.add(birthYearComboBox);
        birthDatePanel.add(new JLabel("年"));

        // 月コンボボックス
        birthMonthComboBox = new JComboBox<>(getMonthOptions());
        birthMonthComboBox.setPreferredSize(new Dimension(60, 25));
        registerComponent("birthMonthComboBox", birthMonthComboBox);
        birthDatePanel.add(birthMonthComboBox);
        birthDatePanel.add(new JLabel("月"));

        // 日コンボボックス
        birthDayComboBox = new JComboBox<>(getDayOptions());
        birthDayComboBox.setPreferredSize(new Dimension(60, 25));
        registerComponent("birthDayComboBox", birthDayComboBox);
        birthDatePanel.add(birthDayComboBox);
        birthDatePanel.add(new JLabel("日"));

        // 生年月日のグループエラー表示用
        createFieldErrorLabel("birthDate");
        container.add(createFormRow(birthDateLabel, birthDatePanel, "birthDate"));
        container.add(createVerticalSpacer(10));

        // 入社年月 - 年・月のコンボボックス
        JLabel joinDateLabel = createFieldLabel("入社年月", true);
        JPanel joinDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        joinDatePanel.setBackground(Color.WHITE);

        // 年コンボボックス
        joinYearComboBox = new JComboBox<>(getYearOptions(1990, LocalDate.now().getYear()));
        joinYearComboBox.setPreferredSize(new Dimension(80, 25));
        registerComponent("joinYearComboBox", joinYearComboBox);
        joinDatePanel.add(joinYearComboBox);
        joinDatePanel.add(new JLabel("年"));

        // 月コンボボックス
        joinMonthComboBox = new JComboBox<>(getMonthOptions());
        joinMonthComboBox.setPreferredSize(new Dimension(60, 25));
        registerComponent("joinMonthComboBox", joinMonthComboBox);
        joinDatePanel.add(joinMonthComboBox);
        joinDatePanel.add(new JLabel("月"));

        // 入社年月のグループエラー表示用
        createFieldErrorLabel("joinDate");
        container.add(createFormRow(joinDateLabel, joinDatePanel, "joinDate"));
        container.add(createVerticalSpacer(10));

        // エンジニア歴（必須）
        JLabel careerLabel = createFieldLabel("エンジニア歴", true);
        JPanel careerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        careerPanel.setBackground(Color.WHITE);

        // エンジニア歴コンボボックス
        careerComboBox = new JComboBox<>(getCareerOptions());
        careerComboBox.setPreferredSize(new Dimension(80, 25));
        registerComponent("careerComboBox", careerComboBox);
        careerPanel.add(careerComboBox);
        careerPanel.add(new JLabel("年"));

        container.add(createFormRow(careerLabel, careerPanel, "careerComboBox"));
        container.add(createVerticalSpacer(20));
    }

    /**
     * 言語スキルセクションの作成
     * プログラミング言語の選択機能をコンボボックス+チェックボックスで実装
     *
     * @param container 配置先のコンテナ
     */
    private void createLanguageSection(JPanel container) {
        // セクションタイトル
        JLabel languageTitle = createSectionTitle("扱える言語");
        JLabel requiredMark = new JLabel(REQUIRED_MARK);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.add(languageTitle);
        titlePanel.add(requiredMark);

        container.add(titlePanel);
        container.add(createVerticalSpacer(10));

        // 言語選択コンボボックスとボタンのパネル
        JPanel languageSelectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        languageSelectPanel.setBackground(Color.WHITE);

        // 利用可能な言語リスト
        String[] availableLanguages = {
                "Java", "Python", "C#", "C++", "JavaScript",
                "TypeScript", "PHP", "Ruby", "Swift", "Kotlin",
                "Go", "Rust", "Scala", "SQL", "HTML/CSS",
                "Dart", "Perl", "R", "COBOL", "Fortran",
                "Lua", "Haskell", "Clojure", "Groovy", "Assembly"
        };

        // 言語選択コンボボックス
        JComboBox<String> languageComboBox = new JComboBox<>(availableLanguages);
        languageComboBox.setPreferredSize(new Dimension(150, 25));
        registerComponent("languageComboBox", languageComboBox);
        languageSelectPanel.add(languageComboBox);

        // 追加ボタン
        JButton addLanguageButton = new JButton("追加");
        addLanguageButton.setPreferredSize(new Dimension(80, 25));
        registerComponent("addLanguageButton", addLanguageButton);
        languageSelectPanel.add(addLanguageButton);

        container.add(languageSelectPanel);
        container.add(createVerticalSpacer(10));

        // 選択済み言語パネル（チェックボックスグループ）
        JPanel selectedLanguagesPanel = new JPanel();
        selectedLanguagesPanel.setLayout(new BoxLayout(selectedLanguagesPanel, BoxLayout.Y_AXIS));
        selectedLanguagesPanel.setBackground(Color.WHITE);
        selectedLanguagesPanel.setBorder(BorderFactory.createTitledBorder("選択済み言語"));
        registerComponent("selectedLanguagesPanel", selectedLanguagesPanel);

        // スクロール可能なパネルに配置
        JScrollPane scrollPane = new JScrollPane(selectedLanguagesPanel);
        scrollPane.setPreferredSize(new Dimension(300, 150));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        container.add(scrollPane);

        // 言語選択のエラー表示用
        createFieldErrorLabel("languages");
        container.add(getFieldErrorLabel("languages"));
        container.add(createVerticalSpacer(10));

        // 言語チェックボックスリストの初期化
        languageCheckBoxes = new ArrayList<>();

        // 追加ボタンのイベント設定
        addLanguageButton.addActionListener(e -> {
            String selectedLanguage = (String) languageComboBox.getSelectedItem();
            if (selectedLanguage != null && !isLanguageSelected(selectedLanguage)) {
                addLanguageCheckbox(selectedLanguage, selectedLanguagesPanel);
                selectedLanguagesPanel.revalidate();
                selectedLanguagesPanel.repaint();

                // エラー表示をクリア（もし表示されていれば）
                clearComponentError("languages");
            }
        });

        container.add(createVerticalSpacer(20));
    }

    /**
     * 言語が既に選択されているかチェック
     *
     * @param language チェックする言語
     * @return 選択済みの場合true
     */
    private boolean isLanguageSelected(String language) {
        for (JCheckBox checkBox : languageCheckBoxes) {
            if (checkBox.getText().equals(language)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 言語チェックボックスを追加
     *
     * @param language 追加する言語
     * @param panel    追加先のパネル
     */
    private void addLanguageCheckbox(String language, JPanel panel) {
        // チェックボックスとボタンを含む行パネル
        JPanel rowPanel = new JPanel(new BorderLayout(5, 0));
        rowPanel.setBackground(Color.WHITE);

        // チェックボックス（デフォルトで選択状態）
        JCheckBox checkBox = new JCheckBox(language, true);
        checkBox.setBackground(Color.WHITE);
        registerComponent("language_" + language, checkBox);
        languageCheckBoxes.add(checkBox);
        rowPanel.add(checkBox, BorderLayout.CENTER);

        // 削除ボタン
        JButton removeButton = new JButton("×");
        removeButton.setPreferredSize(new Dimension(25, 20));
        removeButton.setMargin(new Insets(0, 0, 0, 0));
        removeButton.addActionListener(e -> {
            panel.remove(rowPanel);
            languageCheckBoxes.remove(checkBox);
            panel.revalidate();
            panel.repaint();
        });
        rowPanel.add(removeButton, BorderLayout.EAST);

        // パネルに追加
        panel.add(rowPanel);
    }

    /**
     * 経歴セクションの作成
     * 経歴を入力するテキストエリアを配置
     *
     * @param container 配置先のコンテナ
     */
    private void createCareerHistorySection(JPanel container) {
        // セクションタイトル - 左寄せ
        JLabel careerHistoryTitle = createSectionTitle("経歴");
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.add(careerHistoryTitle);
        container.add(titlePanel);
        container.add(createVerticalSpacer(10));

        // 経歴テキストエリア
        careerHistoryArea = new JTextArea(5, 20);
        careerHistoryArea.setLineWrap(true);
        careerHistoryArea.setWrapStyleWord(true);
        JScrollPane careerScrollPane = new JScrollPane(careerHistoryArea);
        registerComponent("careerHistoryArea", careerHistoryArea);

        container.add(createFormRow(new JLabel(""), careerScrollPane, "careerHistoryArea"));
        container.add(createVerticalSpacer(20));
    }

    /**
     * スキルセクションの作成
     * 技術力、受講態度などのスキル評価用コンボボックスを配置
     *
     * @param container 配置先のコンテナ
     */
    private void createSkillSection(JPanel container) {
        // スキルコンボボックスの共通サイズを定義
        Dimension skillComboBoxSize = new Dimension(80, 25);

        // 技術力
        JLabel technicalSkillLabel = createFieldLabel("技術力", false);
        technicalSkillComboBox = new JComboBox<>(getSkillRatingOptions());

        // コンボボックスをパネルで囲み、サイズを固定
        JPanel techSkillComboPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        techSkillComboPanel.setBackground(Color.WHITE);
        techSkillComboPanel.setPreferredSize(skillComboBoxSize);
        techSkillComboPanel.add(technicalSkillComboBox);

        registerComponent("technicalSkillComboBox", technicalSkillComboBox);
        container.add(createFormRow(technicalSkillLabel, techSkillComboPanel, "technicalSkillComboBox"));
        container.add(createVerticalSpacer(10));

        // 受講態度
        JLabel learningAttitudeLabel = createFieldLabel("受講態度", false);
        learningAttitudeComboBox = new JComboBox<>(getSkillRatingOptions());

        // コンボボックスをパネルで囲み、サイズを固定
        JPanel attitudeComboPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        attitudeComboPanel.setBackground(Color.WHITE);
        attitudeComboPanel.setPreferredSize(skillComboBoxSize);
        attitudeComboPanel.add(learningAttitudeComboBox);

        registerComponent("learningAttitudeComboBox", learningAttitudeComboBox);
        container.add(createFormRow(learningAttitudeLabel, attitudeComboPanel, "learningAttitudeComboBox"));
        container.add(createVerticalSpacer(10));

        // コミュニケーション能力
        JLabel communicationSkillLabel = createFieldLabel("コミュニケーション能力", false);
        communicationSkillComboBox = new JComboBox<>(getSkillRatingOptions());

        // コンボボックスをパネルで囲み、サイズを固定
        JPanel commSkillComboPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        commSkillComboPanel.setBackground(Color.WHITE);
        commSkillComboPanel.setPreferredSize(skillComboBoxSize);
        commSkillComboPanel.add(communicationSkillComboBox);

        registerComponent("communicationSkillComboBox", communicationSkillComboBox);
        container.add(createFormRow(communicationSkillLabel, commSkillComboPanel, "communicationSkillComboBox"));
        container.add(createVerticalSpacer(10));

        // リーダーシップ
        JLabel leadershipLabel = createFieldLabel("リーダーシップ", false);
        leadershipComboBox = new JComboBox<>(getSkillRatingOptions());

        // コンボボックスをパネルで囲み、サイズを固定
        JPanel leadershipComboPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leadershipComboPanel.setBackground(Color.WHITE);
        leadershipComboPanel.setPreferredSize(skillComboBoxSize);
        leadershipComboPanel.add(leadershipComboBox);

        registerComponent("leadershipComboBox", leadershipComboBox);
        container.add(createFormRow(leadershipLabel, leadershipComboPanel, "leadershipComboBox"));
        container.add(createVerticalSpacer(20));
    }

    /**
     * 研修の受講歴セクションの作成
     * 研修の受講歴を入力するテキストエリアを配置
     *
     * @param container 配置先のコンテナ
     */
    private void createTrainingSection(JPanel container) {
        // セクションタイトル - 左寄せ
        JLabel trainingTitle = createSectionTitle("研修の受講歴");
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.add(trainingTitle);
        container.add(titlePanel);
        container.add(createVerticalSpacer(10));

        // 研修の受講歴テキストエリア
        trainingHistoryArea = new JTextArea(5, 20);
        trainingHistoryArea.setLineWrap(true);
        trainingHistoryArea.setWrapStyleWord(true);
        JScrollPane trainingScrollPane = new JScrollPane(trainingHistoryArea);
        registerComponent("trainingHistoryArea", trainingHistoryArea);

        container.add(createFormRow(new JLabel(""), trainingScrollPane, "trainingHistoryArea"));
        container.add(createVerticalSpacer(20));
    }

    /**
     * 備考セクションの作成
     * 備考を入力するテキストエリアを配置
     *
     * @param container 配置先のコンテナ
     */
    private void createNoteSection(JPanel container) {
        // セクションタイトル - 左寄せ
        JLabel noteTitle = createSectionTitle("備考");
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.add(noteTitle);
        container.add(titlePanel);
        container.add(createVerticalSpacer(10));

        // 備考テキストエリア
        noteArea = new JTextArea(5, 20);
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        JScrollPane noteScrollPane = new JScrollPane(noteArea);
        registerComponent("noteArea", noteArea);

        container.add(createFormRow(new JLabel(""), noteScrollPane, "noteArea"));
    }

    /**
     * ボタン領域の作成
     * 「登録」ボタンと「戻る」ボタンを配置
     * 
     * <p>
     * このメソッドは親クラスの{@code buttonPanel}フィールドに操作ボタンを追加します。
     * ローカル変数として新しいパネルを作成せず、親クラスのパネルを直接使用することで、
     * ボタンが正しく表示されるようにします。
     * </p>
     */
    private void createButtonArea() {
        // 処理中表示ラベル
        progressLabel = new JLabel("登録中...");
        progressLabel.setVisible(false);
        addButtonPanelComponent(progressLabel);

        // 戻るボタン
        backButton = new JButton("戻る");
        backButton.addActionListener(e -> {
            if (!processing) {
                goBack();
            }
        });
        addButton(backButton);

        // 登録ボタン
        addButton = new JButton("登録");
        addButton.addActionListener(e -> {
            if (!processing) {
                addEngineer();
            }
        });
        addButton(addButton);
    }

    /**
     * 入力検証の設定
     * フォーカス移動時の入力検証など
     */
    private void setupValidation() {
        // 各フィールドのフォーカス喪失時にバリデーションを実行することもできます
        // 例：
        /*
         * nameField.addFocusListener(new FocusAdapter() {
         * 
         * @Override
         * public void focusLost(FocusEvent e) {
         * validateTextField("nameField", true, 20, null,
         * MessageEnum.VALIDATION_ERROR_NAME.getMessage());
         * }
         * });
         */

        // この実装ではフォーカス時のバリデーションは行わず、登録ボタン押下時に一括検証を行います
    }

    /**
     * 登録ボタンのクリックイベント処理
     * データの検証、エンジニア情報の構築、保存処理を実行
     */
    private void addEngineer() {
        try {
            // エラーメッセージのクリア
            clearAllComponentErrors();

            // 入力検証
            if (!validateInput()) {
                // エラーメッセージは validateInput() 内で表示されます
                return;
            }

            // 処理中状態に設定
            setProcessing(true);

            // エンジニア情報の構築
            EngineerDTO engineer = buildEngineerDTO();

            // 保存処理の実行（非同期）
            if (mainController != null) {
                // 保存イベント発行
                mainController.handleEvent("SAVE_DATA", engineer);

                // 注意: 処理中状態の解除は handleSaveComplete で行うため、ここでは行わない
            } else {
                // コントローラー未設定エラー
                showErrorMessage("システムエラー: コントローラーが設定されていません");
                // 処理中状態の解除
                setProcessing(false);
            }

        } catch (Exception e) {
            LogHandler.getInstance().logError(LogType.UI, "エンジニア追加処理中にエラーが発生しました", e);
            showErrorMessage("エンジニア情報の登録中にエラーが発生しました: " + e.getMessage());
            // 処理中状態の解除
            setProcessing(false);
        }
    }

    /**
     * 入力検証を実行
     * 各入力フィールドの値を検証し、エラーがあればフィールドごとにエラーメッセージを表示
     * 
     * <p>
     * バージョン4.1.0での改善点:
     * <ul>
     * <li>複数のエラーを同時に検出して表示</li>
     * <li>フィールドごとにエラーメッセージを表示</li>
     * <li>最初のエラーフィールドへのフォーカス設定</li>
     * </ul>
     * </p>
     *
     * @return 検証成功の場合true、失敗の場合false
     */
    @Override
    protected boolean validateInput() {
        // 検証結果フラグ
        boolean isValid = true;

        // コンポーネントエラーをクリア
        clearAllComponentErrors();

        // 氏名の検証
        if (isEmpty(nameField)) {
            showFieldError("nameField", MessageEnum.VALIDATION_ERROR_NAME.getMessage());
            isValid = false;
        } else if (nameField.getText().length() > 20) {
            showFieldError("nameField", MessageEnum.VALIDATION_ERROR_NAME.getMessage());
            isValid = false;
        }

        // 社員IDの検証
        if (isEmpty(idField)) {
            showFieldError("idField", MessageEnum.VALIDATION_ERROR_EMPLOYEE_ID.getMessage());
            isValid = false;
        } else if (!idField.getText().matches("ID\\d{5}")) {
            showFieldError("idField", MessageEnum.VALIDATION_ERROR_EMPLOYEE_ID.getMessage());
            isValid = false;
        } else if (idField.getText().equals("ID00000") || idField.getText().equals("00000")) {
            showFieldError("idField", "ID00000は使用できません");
            isValid = false;
        }
        // フリガナの検証
        Validator kanaValidator = ValidatorEnum.NAME_KANA.getValidator();
        if (!kanaValidator.validate(nameKanaField.getText())) {
            showFieldError("nameKanaField", kanaValidator.getErrorMessage());
            isValid = false;
        }

        // 生年月日の検証
        if (!validateDateComponents("birthYearComboBox", "birthMonthComboBox", "birthDayComboBox",
                "birthDate", true, MessageEnum.VALIDATION_ERROR_BIRTH_DATE.getMessage())) {
            isValid = false;
        }

        // 入社年月の検証
        if (!validateDateComponents("joinYearComboBox", "joinMonthComboBox", null,
                "joinDate", true, MessageEnum.VALIDATION_ERROR_JOIN_DATE.getMessage())) {
            isValid = false;
        }

        // エンジニア歴の検証
        if (isEmptyComboBox(careerComboBox)) {
            showFieldError("careerComboBox", MessageEnum.VALIDATION_ERROR_CAREER.getMessage());
            isValid = false;
        }

        // 扱える言語の検証（1つ以上選択）
        boolean hasLanguage = false;
        for (JCheckBox checkBox : languageCheckBoxes) {
            if (checkBox.isSelected()) {
                hasLanguage = true;
                break;
            }
        }
        if (!hasLanguage) {
            showFieldError("languages", MessageEnum.VALIDATION_ERROR_PROGRAMMING_LANGUAGES.getMessage());
            isValid = false;
        }

        // 経歴の文字数検証（200文字以内）
        if (careerHistoryArea.getText().length() > 200) {
            showFieldError("careerHistoryArea", MessageEnum.VALIDATION_ERROR_CAREER_HISTORY.getMessage());
            isValid = false;
        }

        // 研修の受講歴の文字数検証（200文字以内）
        if (trainingHistoryArea.getText().length() > 200) {
            showFieldError("trainingHistoryArea", MessageEnum.VALIDATION_ERROR_TRAINING_HISTORY.getMessage());
            isValid = false;
        }

        // 備考の文字数検証（500文字以内）
        if (noteArea.getText().length() > 500) {
            showFieldError("noteArea", MessageEnum.VALIDATION_ERROR_NOTE.getMessage());
            isValid = false;
        }

        // 検証に失敗した場合、最初のエラーフィールドにフォーカスを設定
        if (!isValid && !errorComponents.isEmpty()) {
            // エラーコンポーネントの最初の要素を取得
            Component firstErrorComponent = errorComponents.values().iterator().next();

            // フォーカス可能なコンポーネントかどうか確認
            if (firstErrorComponent instanceof JComponent) {
                JComponent jComponent = (JComponent) firstErrorComponent;

                // フォーカスを設定
                jComponent.requestFocusInWindow();
            }
        }

        return isValid;
    }

    /**
     * エンジニア情報DTOを構築
     * 入力フィールドの値を取得してEngineerDTOオブジェクトを構築
     * 
     * <p>
     * このメソッドは入力フォームの各フィールドから値を取得し、EngineerBuilderを使用して
     * EngineerDTOオブジェクトを生成します。事前にバリデーションが行われていることを前提としています。
     * </p>
     *
     * @return 構築したEngineerDTOオブジェクト
     */
    private EngineerDTO buildEngineerDTO() {
        // EngineerBuilderを使用してDTOを構築
        EngineerBuilder builder = new EngineerBuilder();

        // 基本情報の設定
        builder.setId(idField.getText().trim());
        builder.setName(nameField.getText().trim());
        builder.setNameKana(nameKanaField.getText().trim());

        // 生年月日の設定
        LocalDate birthDate = getDateFromComponents(
                birthYearComboBox, birthMonthComboBox, birthDayComboBox);
        if (birthDate != null) {
            builder.setBirthDate(birthDate);
        }

        // 入社年月の設定
        LocalDate joinDate = getDateFromComponents(
                joinYearComboBox, joinMonthComboBox, null);
        if (joinDate != null) {
            builder.setJoinDate(joinDate);
        }

        // エンジニア歴の設定
        String careerText = (String) careerComboBox.getSelectedItem();
        if (careerText != null && !careerText.isEmpty()) {
            try {
                int career = Integer.parseInt(careerText);
                builder.setCareer(career);
            } catch (NumberFormatException e) {
                builder.setCareer(0);
            }
        } else {
            builder.setCareer(0);
        }

        // 扱える言語の設定
        List<String> languages = getSelectedLanguages();
        builder.setProgrammingLanguages(languages);

        // 経歴の設定
        String careerHistory = careerHistoryArea.getText().trim();
        if (!careerHistory.isEmpty()) {
            builder.setCareerHistory(careerHistory);
        }

        // 研修の受講歴の設定
        String trainingHistory = trainingHistoryArea.getText().trim();
        if (!trainingHistory.isEmpty()) {
            builder.setTrainingHistory(trainingHistory);
        }

        // スキル評価の設定
        setSkillRating(builder);

        // 備考の設定
        String note = noteArea.getText().trim();
        if (!note.isEmpty()) {
            builder.setNote(note);
        }

        // 登録日時は自動設定（current date）

        // DTOの構築と返却
        return builder.build();
    }

    /**
     * スキル評価をビルダーに設定
     * コンボボックスから選択された評価値をビルダーに設定
     *
     * @param builder エンジニアビルダー
     */
    private void setSkillRating(EngineerBuilder builder) {
        // 技術力
        String technicalSkill = (String) technicalSkillComboBox.getSelectedItem();
        if (technicalSkill != null && !technicalSkill.isEmpty()) {
            try {
                double skill = Double.parseDouble(technicalSkill);
                builder.setTechnicalSkill(skill);
            } catch (NumberFormatException e) {
                // デフォルト値（3.0）を使用
                builder.setTechnicalSkill(3.0);
            }
        }

        // 受講態度
        String learningAttitude = (String) learningAttitudeComboBox.getSelectedItem();
        if (learningAttitude != null && !learningAttitude.isEmpty()) {
            try {
                double attitude = Double.parseDouble(learningAttitude);
                builder.setLearningAttitude(attitude);
            } catch (NumberFormatException e) {
                // デフォルト値（3.0）を使用
                builder.setLearningAttitude(3.0);
            }
        }

        // コミュニケーション能力
        String communicationSkill = (String) communicationSkillComboBox.getSelectedItem();
        if (communicationSkill != null && !communicationSkill.isEmpty()) {
            try {
                double skill = Double.parseDouble(communicationSkill);
                builder.setCommunicationSkill(skill);
            } catch (NumberFormatException e) {
                // デフォルト値（3.0）を使用
                builder.setCommunicationSkill(3.0);
            }
        }

        // リーダーシップ
        String leadership = (String) leadershipComboBox.getSelectedItem();
        if (leadership != null && !leadership.isEmpty()) {
            try {
                double lead = Double.parseDouble(leadership);
                builder.setLeadership(lead);
            } catch (NumberFormatException e) {
                // デフォルト値（3.0）を使用
                builder.setLeadership(3.0);
            }
        }
    }

    /**
     * 選択された言語のリストを取得
     * チェックされた言語チェックボックスから言語名を取得
     *
     * @return 選択された言語のリスト
     */
    private List<String> getSelectedLanguages() {
        List<String> selectedLanguages = new ArrayList<>();

        for (JCheckBox checkBox : languageCheckBoxes) {
            if (checkBox.isSelected()) {
                selectedLanguages.add(checkBox.getText());
            }
        }

        return selectedLanguages;
    }

    /**
     * コンボボックスから日付オブジェクトを取得
     * 年・月・日のコンボボックスから日付を構築
     *
     * @param yearComboBox  年コンボボックス
     * @param monthComboBox 月コンボボックス
     * @param dayComboBox   日コンボボックス（null可）
     * @return 構築した日付、無効な場合はnull
     */
    private LocalDate getDateFromComponents(JComboBox<String> yearComboBox,
            JComboBox<String> monthComboBox,
            JComboBox<String> dayComboBox) {
        try {
            String yearStr = (String) yearComboBox.getSelectedItem();
            String monthStr = (String) monthComboBox.getSelectedItem();

            if (yearStr == null || yearStr.isEmpty() || monthStr == null || monthStr.isEmpty()) {
                return null;
            }

            int year = Integer.parseInt(yearStr);
            int month = Integer.parseInt(monthStr);

            // 日コンボボックスがある場合（生年月日）
            if (dayComboBox != null) {
                String dayStr = (String) dayComboBox.getSelectedItem();
                if (dayStr == null || dayStr.isEmpty()) {
                    return null;
                }
                int day = Integer.parseInt(dayStr);
                return LocalDate.of(year, month, day);
            }

            // 日コンボボックスがない場合（入社年月）
            return LocalDate.of(year, month, 1);

        } catch (NumberFormatException | java.time.DateTimeException e) {
            // 無効な日付の場合
            return null;
        }
    }

    /**
     * 完了処理の成功状態を取得します
     * <p>
     * このメソッドは、handleSaveCompleteメソッドが正常に完了したかどうかを
     * 返します。MainControllerなど外部クラスからこの情報を取得することで、
     * 必要に応じて代替処理を実行できます。
     * </p>
     * 
     * @return 完了処理が成功した場合true、そうでなければfalse
     */
    public boolean isHandleSaveCompleteSuccess() {
        return handleSaveCompleteSuccess;
    }

    /**
     * 現在の処理中状態を取得します
     * <p>
     * このメソッドは、パネルが現在処理中状態（登録処理実行中など）かどうかを
     * 返します。この情報は外部クラスが適切な処理を判断するために使用できます。
     * </p>
     * 
     * @return 処理中の場合true、そうでなければfalse
     */
    public boolean isProcessing() {
        return this.processing;
    }

    /**
     * 保存完了処理を実行します
     * <p>
     * このメソッドは、エンジニア情報の保存処理が正常に完了した後に呼び出されます。
     * 主な役割は以下の通りです：
     * </p>
     * 
     * <ol>
     * <li>処理中状態の解除（プログレスインジケーターの非表示化）</li>
     * <li>登録完了ダイアログの表示と次のアクション選択の提供</li>
     * <li>選択されたアクションに基づく画面遷移またはフォームクリア</li>
     * </ol>
     * 
     * <p>
     * ユーザーには次の3つのアクションが提供されます：
     * </p>
     * 
     * <ul>
     * <li><b>続けて登録</b>: フォームをクリアして新たな登録を続行</li>
     * <li><b>一覧に戻る</b>: エンジニア一覧画面に遷移</li>
     * <li><b>詳細を表示</b>: 登録したエンジニアの詳細画面に遷移</li>
     * </ul>
     * 
     * <p>
     * このメソッドは例外処理を強化し、処理中に問題が発生しても確実に処理中状態を解除し、
     * ログに詳細を記録します。また、処理の各段階でのログ出力により、デバッグや問題追跡が
     * 容易になっています。
     * </p>
     * 
     * <p>
     * 本メソッドは非同期処理の完了後にSwingのEDT（Event Dispatch Thread）上で
     * 呼び出されることを前提としています。
     * </p>
     * 
     * @param engineer 保存されたエンジニア情報（{@link EngineerDTO}オブジェクト）
     */
    public void handleSaveComplete(EngineerDTO engineer) {
        LogHandler.getInstance().log(Level.INFO, LogType.SYSTEM,
                "AddPanel.handleSaveComplete開始: ID=" + engineer.getId());

        try {
            // 処理中状態を解除（プログレスインジケーターの非表示化とUI操作の有効化）
            setProcessing(false);
            LogHandler.getInstance().log(Level.INFO, LogType.SYSTEM,
                    "処理中状態を解除しました");

            // 登録成功後のダイアログ表示と画面遷移処理
            LogHandler.getInstance().log(Level.INFO, LogType.SYSTEM,
                    "登録完了ダイアログを表示します: ID=" + engineer.getId());

            // 登録完了ダイアログを表示し、次のアクションを取得
            String action = dialogManager.showRegisterCompletionDialog(engineer);
            LogHandler.getInstance().log(Level.INFO, LogType.SYSTEM,
                    "選択されたアクション: " + action);

            // 選択されたアクションに応じた処理
            switch (action) {
                case "CONTINUE":
                    // フォームをクリアして現在のページに留まる
                    LogHandler.getInstance().log(Level.INFO, LogType.SYSTEM,
                            "「続けて登録」が選択されました - フォームをクリアします");
                    clearFields();
                    break;

                case "LIST":
                    // 一覧画面に戻る
                    LogHandler.getInstance().log(Level.INFO, LogType.SYSTEM,
                            "「一覧に戻る」が選択されました - 一覧画面に遷移します");
                    if (mainController != null) {
                        // データ再読込を実行してから画面遷移
                        mainController.handleEvent("LOAD_DATA", null);
                        // 画面遷移
                        mainController.handleEvent("CHANGE_PANEL", "LIST");
                    } else {
                        LogHandler.getInstance().log(Level.WARNING, LogType.SYSTEM,
                                "MainControllerが設定されていないため画面遷移できません");
                        // フォールバック処理（コントローラーが利用できない場合）
                        clearFields();
                    }
                    break;

                case "DETAIL":
                    // 登録したエンジニアの詳細画面に遷移
                    LogHandler.getInstance().log(Level.INFO, LogType.SYSTEM,
                            "「詳細を表示」が選択されました - 詳細画面に遷移します: ID=" + engineer.getId());
                    if (mainController != null) {
                        mainController.handleEvent("VIEW_DETAIL", engineer.getId());
                    } else {
                        LogHandler.getInstance().log(Level.WARNING, LogType.SYSTEM,
                                "MainControllerが設定されていないため画面遷移できません");
                        // フォールバック処理（コントローラーが利用できない場合）
                        clearFields();
                    }
                    break;

                default:
                    // 未知のアクション（通常は発生しない）
                    LogHandler.getInstance().log(Level.WARNING, LogType.SYSTEM,
                            "未知のアクションが選択されました: " + action + " - デフォルト処理を実行します");
                    clearFields();
                    break;
            }

            // 処理成功フラグを設定
            handleSaveCompleteSuccess = true;
            LogHandler.getInstance().log(Level.INFO, LogType.SYSTEM,
                    "AddPanel.handleSaveComplete完了: ID=" + engineer.getId());

        } catch (Exception e) {
            // 例外が発生した場合のエラーハンドリング
            LogHandler.getInstance().logError(LogType.SYSTEM,
                    "handleSaveComplete処理中にエラーが発生しました: " + engineer.getId(), e);

            // UIスレッドでエラーダイアログを表示
            try {
                DialogManager.getInstance().showErrorDialog(
                        "エラー",
                        "登録完了処理中にエラーが発生しました: " + e.getMessage());
            } catch (Exception dialogError) {
                // ダイアログ表示自体が失敗した場合
                LogHandler.getInstance().logError(LogType.SYSTEM,
                        "エラーダイアログの表示にも失敗しました", dialogError);
            }

            // 処理中状態を強制的に解除（重要: UIがブロックされないようにする）
            setProcessing(false);

            // 成功フラグはfalseのまま
            handleSaveCompleteSuccess = false;
        } finally {
            // 最終的な状態確認とクリーンアップ処理
            if (processing) {
                // 万が一まだ処理中状態が解除されていない場合の保険
                LogHandler.getInstance().log(Level.WARNING, LogType.SYSTEM,
                        "処理中状態が解除されていません - 強制的に解除します");
                setProcessing(false);
            }
        }
    }

    /**
     * 入力フィールドをクリア
     * すべての入力フィールドを初期状態にリセットし、エラー表示もクリアします
     */
    private void clearFields() {
        // テキストフィールドのクリア
        nameField.setText("");
        idField.setText("");
        nameKanaField.setText("");

        // テキストエリアのクリア
        careerHistoryArea.setText("");
        trainingHistoryArea.setText("");
        noteArea.setText("");

        // コンボボックスのリセット
        birthYearComboBox.setSelectedIndex(0);
        birthMonthComboBox.setSelectedIndex(0);
        birthDayComboBox.setSelectedIndex(0);
        joinYearComboBox.setSelectedIndex(0);
        joinMonthComboBox.setSelectedIndex(0);
        careerComboBox.setSelectedIndex(0);
        technicalSkillComboBox.setSelectedIndex(0);
        learningAttitudeComboBox.setSelectedIndex(0);
        communicationSkillComboBox.setSelectedIndex(0);
        leadershipComboBox.setSelectedIndex(0);

        // チェックボックスのクリア
        for (JCheckBox checkBox : languageCheckBoxes) {
            checkBox.setSelected(false);
        }

        // フォーカスを名前フィールドに設定
        nameField.requestFocus();

        // エラーメッセージのクリア
        clearAllComponentErrors();
    }

    /**
     * 一覧画面に戻る
     * コントローラーを通じて画面遷移を行う
     */
    private void goBack() {
        // 入力フィールドをクリア
        clearFields();

        if (mainController != null) {
            mainController.handleEvent("CHANGE_PANEL", "LIST");
            LogHandler.getInstance().log(Level.INFO, LogType.UI, "一覧画面に戻ります");
        } else {
            LogHandler.getInstance().log(Level.WARNING, LogType.UI,
                    "MainControllerが設定されていないため画面遷移できません");
        }
    }

    /**
     * 処理中状態の設定
     * 処理中はUIコンポーネントを無効化し、プログレスインジケーターを表示します
     *
     * @param processing 処理中の場合true
     */
    public void setProcessing(boolean processing) {
        this.processing = processing;

        // UIコンポーネントの有効/無効を切り替え
        setAllComponentsEnabled(!processing);

        // ボタンの有効/無効を切り替え
        addButton.setEnabled(!processing);
        backButton.setEnabled(!processing);

        // プログレスインジケーターの表示/非表示を切り替え
        progressLabel.setVisible(processing);
    }

    /**
     * 年の選択肢を取得
     *
     * @param startYear 開始年
     * @param endYear   終了年
     * @return 年の選択肢配列
     */
    private String[] getYearOptions(int startYear, int endYear) {
        List<String> years = new ArrayList<>();
        years.add(""); // 空の選択肢

        for (int year = startYear; year <= endYear; year++) {
            years.add(String.valueOf(year));
        }

        return years.toArray(new String[0]);
    }

    /**
     * 月の選択肢を取得
     *
     * @return 月の選択肢配列
     */
    private String[] getMonthOptions() {
        String[] months = new String[13]; // 空 + 1-12月
        months[0] = "";

        for (int i = 1; i <= 12; i++) {
            months[i] = String.valueOf(i);
        }

        return months;
    }

    /**
     * 日の選択肢を取得
     *
     * @return 日の選択肢配列
     */
    private String[] getDayOptions() {
        String[] days = new String[32]; // 空 + 1-31日
        days[0] = "";

        for (int i = 1; i <= 31; i++) {
            days[i] = String.valueOf(i);
        }

        return days;
    }

    /**
     * エンジニア歴の選択肢を取得
     *
     * @return エンジニア歴の選択肢配列
     */
    private String[] getCareerOptions() {
        String[] careers = new String[21]; // 空 + 0-19年
        careers[0] = "";

        for (int i = 0; i <= 19; i++) {
            careers[i + 1] = String.valueOf(i);
        }

        return careers;
    }

    /**
     * スキル評価の選択肢を取得（1.0-5.0、0.5刻み）
     *
     * @return スキル評価の選択肢配列
     */
    private String[] getSkillRatingOptions() {
        String[] ratings = new String[10]; // 空 + 1.0-5.0（0.5刻み）
        ratings[0] = "";

        double rating = 1.0;
        for (int i = 1; i < ratings.length; i++) {
            ratings[i] = String.valueOf(rating);
            rating += 0.5;
        }

        return ratings;
    }
}