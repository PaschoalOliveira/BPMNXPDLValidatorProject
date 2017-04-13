package bpmnxpdlvalidator;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MetricCalculator {

    private Document document = null;
    private static Boolean ccMetricFound = false;
    private static Boolean cmMetricFound = false;
    InitialFrame initialFrame;
    ProgressBarDemo progressBarDemo;

    public MetricCalculator(Document doc, InitialFrame initialFrame) {

        this.document = doc;
        this.initialFrame = initialFrame;

    }

    public MetricCalculator(Document doc, ProgressBarDemo progressBarDemo) {

        this.document = doc;
        this.progressBarDemo = progressBarDemo;

    }

    //TODO - Deve-se calcular as entradas nos pontos. Isto � utilizado no calculo CM. 
    public void calculateMetrics() {

        int andCount = 0;
        int orCount = 0;
        int xorCount = 0;

        int outPutAnd = 0;
        int outPutOr = 0;
        int outPutXor = 0;

        andCount = findNodesWithAND(this.document).size();
        orCount = findNodesWithOR(this.document).size();
        xorCount = findNodesWithXOR(this.document).size();

        outPutAnd = calculateOutPut(findNodesWithAND(this.document));
        outPutOr = calculateOutPut(findNodesWithOR(this.document));
        outPutXor = calculateOutPut(findNodesWithXOR(this.document));

        Long lCFCMetricValue = this.calculateCFC(andCount, orCount, xorCount, outPutAnd, outPutOr, outPutXor);
        System.out.println("CFC Metric = " + lCFCMetricValue);
        this.initialFrame.getjTextFielCFCResult().setText(lCFCMetricValue.toString());
        this.initialFrame.getjTextFieldThreesholdCFC().setText(this.calculateCFCThresholdClass(lCFCMetricValue));
        //this.progressBarDemo.setProgressBarValue(50);

        this.initialFrame.repaint();

        //this.progressBar.setValue(10);
        //this.initialFrame.atualizarProgressBar(10);
        InitialFrame.progressBarValue = 20;
        //this.initialFrame.getTask().execute();
        //this.initialFrame.repaint();

        this.initialFrame.repaint();

        DecimalFormat df = new DecimalFormat("#.##########");
        df.setRoundingMode(RoundingMode.CEILING);

        Double dCMMetricValue = this.calculateCM();
        System.out.println("CM Metric = " + dCMMetricValue);
        this.initialFrame.getjTextFielCMResult().setText(df.format(dCMMetricValue).toString());
        this.initialFrame.getjTextFieldThreesholdCM().setText(this.calculateCMThresholdClass(dCMMetricValue));

        InitialFrame.progressBarValue = 30;
        //this.initialFrame.getTask().execute();
        //this.initialFrame.repaint();

        Double lCCMetricValue = this.calculateCC();
        System.out.println("CC Metric = " + lCCMetricValue);
        this.initialFrame.getjTextFielCCResult().setText(lCCMetricValue.toString());

        //this.initialFrame.atualizarProgressBar(70);
        InitialFrame.progressBarValue = 70;
        //this.initialFrame.getTask().execute();
        //this.initialFrame.repaint();

        Double newCCMetricValue = this.newCalculateCC();
        System.out.println("new CC Metric = " + newCCMetricValue);
        this.initialFrame.getjTextFielCCResult().setText(df.format(newCCMetricValue).toString());
        this.initialFrame.getjTextFieldThreesholdCC().setText(this.calculateCCThresholdClass(newCCMetricValue));

        //this.initialFrame.atualizarProgressBar(70);
        InitialFrame.progressBarValue = 100;
        //this.initialFrame.getTask().execute();
        //this.initialFrame.repaint();
    }

    /*
        [0, 1) Class A
        [1, 4) Class B
        [4, 6) Class C
        [6, 11) Class D
        [11, ∞) Class E
     */
    private String calculateCFCThresholdClass(Long value) {

        String sClass = "";
        if (value >= 0 && value < 1) {
            sClass = "Class A";
        } else if (value >= 1 && value < 4) {
            sClass = "Class B";
        } else if (value >= 4 && value < 6) {
            sClass = "Class C";
        } else if (value >= 6 && value < 11) {
            sClass = "Class D";
        } else if (value >= 11) {
            sClass = "Class E";
        }
        return sClass;
    }


    /*
        [0.0, 0.003934) Class A
[0.003934, 0.064361) Class B
[0.064361, 0.114167) Class C
[0.114167, 0.176786) Class D
[0.176786, ∞) Class E
     */
    private String calculateCMThresholdClass(Double value) {

        String sClass = "";
        if (value >= 0 && value < 0.003934) {
            sClass = "Class A";
        } else if (value >= 0.003934 && value < 0.064361) {
            sClass = "Class B";
        } else if (value >= 0.064361 && value < 0.114167) {
            sClass = "Class C";
        } else if (value >= 0.114167 && value < 0.176786) {
            sClass = "Class D";
        } else if (value >= 0.176786) {
            sClass = "Class E";
        }
        return sClass;
    }

    /*
        [0, 0.007996) Class E
[0.007996, 0.030407) Class D
[0.030407, 0.061814) Class C
[0.061814, 0.112903) Class B
[0.112903, ∞) Class A
     */
    private String calculateCCThresholdClass(Double value) {

        String sClass = "";
        if (value >= 0 && value < 0.007996) {
            sClass = "Class E";
        } else if (value >= 0.007996 && value < 0.030407) {
            sClass = "Class D";
        } else if (value >= 0.030407 && value < 0.061814) {
            sClass = "Class C";
        } else if (value >= 0.061814 && value < 0.112903) {
            sClass = "Class B";
        } else if (value >= 0.112903) {
            sClass = "Class A";
        }
        return sClass;
    }

    //TODO - investigar se esta métrica esta corretamente implementada de acordo com o artigo
    private long calculateCFC(int andCount, int orCount, int xorCount, int outPutAnd, int outPutOr, int outPutXor) {

        long CFCAndValue = 0;
        long CFCOrValue = 0;
        long CFCXorValue = 0;

        CFCAndValue = andCount;
        CFCOrValue = (long) (Math.pow(2, outPutOr) - 1);
        CFCXorValue = outPutXor;

        return CFCAndValue + CFCOrValue + CFCXorValue;
    }

    //TODO -deve-se adicionar também ao calculo final quando não há ligação de AND, OR ou XOr. Ou seja, quando uma atividade está ligada diretamente a outra
    private double calculateCM() {

        Map<String, Map<String, Double>> treeOfValues = new HashMap<String, Map<String, Double>>();

        ArrayList<String> arrayIdActivities = this.findOnlyIdActivitiesElements(this.document);

        for (int x = 0; x < arrayIdActivities.size(); x++) {
            String idOrigem = arrayIdActivities.get(x);
            Map<String, Double> treeOfDestinos = new HashMap<String, Double>();

            for (int y = 0; y < arrayIdActivities.size(); y++) {
                cmMetricFound = false;
                String idDestino = arrayIdActivities.get(y);
                if (idOrigem.equals(idDestino)) {
                    treeOfDestinos.put(idDestino, 0.00);
                } else {
                    ArrayList<String> idsOrigem = new ArrayList<String>();
                    idsOrigem.add(idOrigem);
                    Double CMValue = this.calculateCMPath(idsOrigem, idDestino, 1.00);
                    treeOfDestinos.put(idDestino, CMValue);
                }
            }
            treeOfValues.put(idOrigem, treeOfDestinos);
        }

        Double totalValue = 0.0;
        for (Map.Entry<String, Map<String, Double>> entryMap : treeOfValues.entrySet()) {
            String idOrigem = entryMap.getKey();
            Map<String, Double> treeIdDestinoAndValor = entryMap.getValue();
            for (Map.Entry<String, Double> entryMapDestino : treeIdDestinoAndValor.entrySet()) {
                String idDestino = entryMapDestino.getKey();
                Double value = entryMapDestino.getValue();

                totalValue = totalValue + value;
                if (value != 0) {
                    System.out.println("Origem: " + this.getNameById(idOrigem) + " Destino: " + this.getNameById(idDestino) + " Valor: " + value);
                }
            }
        }
        int totalnumberOfActivities = arrayIdActivities.size();

        //Calculo final da métrica CM
        return totalValue / (totalnumberOfActivities * (totalnumberOfActivities - 1));

    }

    private Double newCalculateCC() {

        Map<String, Map<String, Double>> treeOfElements = this.createTreeOfValues();

        int countOfElements = 0;
        
        NodeList nodeListAInvestigar = this.document.getElementsByTagName("Transition");
        //Percorre a arvore de origem e destino atribuindo os respectivos valores a cada um deles
        for (Map.Entry<String, Map<String, Double>> entryMap : treeOfElements.entrySet()) {
            String idOrigem = entryMap.getKey();
            Map<String, Double> treeIdDestinoAndValor = entryMap.getValue();

            System.out.println("Count Of Elements" + countOfElements);
            for (Map.Entry<String, Double> entryMapDestino : treeIdDestinoAndValor.entrySet()) {
                Double dPathValue = 1.0;

                String idDestino = entryMapDestino.getKey();

                HashMap<String, Double> listOrigem = new HashMap<String, Double>();
                listOrigem.put(idOrigem, 1.0);

                HashMap<String, Double> listOrigem2 = new HashMap<String, Double>();
                listOrigem2.put(idOrigem, 1.0);

                HashMap<String, String> listNodesCovered = new HashMap<String, String>();

                //Coloca como falso o booleano que indica que foi achado um caminho entre a origem e o destino especificado a cada loop
                this.ccMetricFound = false;
                HashMap<String, HashMap<String, Double>> hashFinal = new HashMap<String, HashMap<String, Double>>();

                String sOrigem = this.getNameById(idOrigem);
                String sDestino = this.getNameById(idDestino);

                dPathValue = this.newCalculateCCPath(nodeListAInvestigar, dPathValue, listOrigem, idDestino, listNodesCovered, hashFinal);

                treeIdDestinoAndValor.put(idDestino, dPathValue);
            }

            treeOfElements.put(idOrigem, treeIdDestinoAndValor);
            
            countOfElements++;
        }

        Double totalValue = 0.0;
        for (Map.Entry<String, Map<String, Double>> entryMap : treeOfElements.entrySet()) {
            String idOrigem = entryMap.getKey();
            Map<String, Double> treeIdDestinoAndValor = entryMap.getValue();
            for (Map.Entry<String, Double> entryMapDestino : treeIdDestinoAndValor.entrySet()) {
                String idDestino = entryMapDestino.getKey();
                Double value = entryMapDestino.getValue();

                totalValue = totalValue + value;
                if (value != 0.0) {

                    /*
					if((this.getNameById(idOrigem).equals("Inicio") || this.getNameById(idOrigem).equals("Tarefa 1"))
							&&
							(this.getNameById(idDestino).equals("Tarefa  7") || this.getNameById(idDestino).equals("Fim"))){
						System.out.println("Origem: " + this.getNameById(idOrigem) + " Destino: " + this.getNameById(idDestino) + " Valor: " + value);
					}
                     */
                    System.out.println("Origem: " + this.getNameById(idOrigem) + " Destino: " + this.getNameById(idDestino) + " Valor: " + value);
                }
            }
        }
        int totalnumberOfElements = this.findAllIdElements(this.document).size();

        //Calculo final da métrica CC
        return totalValue / (totalnumberOfElements * (totalnumberOfElements - 1));
    }

    //TODO - retirar variaveis duplicadas
    private Double calculateCC() {

        Map<String, Map<String, Double>> treeOfElements = this.createTreeOfValues();

        NodeList nodeListAInvestigar = this.document.getElementsByTagName("Transition");

        //Percorre a arvore de origem e destino atribuindo os respectivos valores a cada um deles
        for (Map.Entry<String, Map<String, Double>> entryMap : treeOfElements.entrySet()) {
            String idOrigem = entryMap.getKey();
            Map<String, Double> treeIdDestinoAndValor = entryMap.getValue();

            for (Map.Entry<String, Double> entryMapDestino : treeIdDestinoAndValor.entrySet()) {
                Double dPathValue = 1.0;
                Double dPathValue2 = 1.0;

                String idDestino = entryMapDestino.getKey();

                HashMap<String, Double> listOrigem = new HashMap<String, Double>();
                listOrigem.put(idOrigem, 1.0);

                HashMap<String, Double> listOrigem2 = new HashMap<String, Double>();
                listOrigem2.put(idOrigem, 1.0);

                HashMap<String, String> listNodesCovered = new HashMap<String, String>();

                //Coloca como falso o booleano que indica que foi achado um caminho entre a origem e o destino especificado a cada loop
                this.ccMetricFound = false;
                HashMap<String, HashMap<String, Double>> hashFinal = new HashMap<String, HashMap<String, Double>>();

                String sOrigem = this.getNameById(idOrigem);
                String sDestino = this.getNameById(idDestino);

                dPathValue = this.calculateCCPath(nodeListAInvestigar, dPathValue, listOrigem, idDestino, listNodesCovered);

                //dPathValue2 = this.newCalculateCCPath(nodeListAInvestigar2, dPathValue2, listOrigem2, idDestino,listNodesCovered2, hashFinal);
                treeIdDestinoAndValor.put(idDestino, dPathValue);

            }

            treeOfElements.put(idOrigem, treeIdDestinoAndValor);
        }

        Double totalValue = 0.0;
        for (Map.Entry<String, Map<String, Double>> entryMap : treeOfElements.entrySet()) {
            String idOrigem = entryMap.getKey();
            Map<String, Double> treeIdDestinoAndValor = entryMap.getValue();
            for (Map.Entry<String, Double> entryMapDestino : treeIdDestinoAndValor.entrySet()) {
                String idDestino = entryMapDestino.getKey();
                Double value = entryMapDestino.getValue();

                totalValue = totalValue + value;

                if (value != 0.0) {

                    /*
                            if((this.getNameById(idOrigem).equals("Inicio") || this.getNameById(idOrigem).equals("Tarefa 1"))
                                                    &&
                                                    (this.getNameById(idDestino).equals("Tarefa  7") || this.getNameById(idDestino).equals("Fim"))){
                                    System.out.println("Origem: " + this.getNameById(idOrigem) + " Destino: " + this.getNameById(idDestino) + " Valor: " + value);
                            }
                     */
                    System.out.println("Origem: " + this.getNameById(idOrigem) + " Destino: " + this.getNameById(idDestino) + " Valor: " + value);
                }
            }
        }
        int totalnumberOfElements = this.findAllIdElements(this.document).size();

        //Calculo final da métrica CC
        return totalValue / (totalnumberOfElements * (totalnumberOfElements - 1));
    }

    private String getNameById(String id) {

        String name = "";
        NodeList nElementsTaskList = this.document.getElementsByTagName("Activity");

        for (int x = 0; x < nElementsTaskList.getLength(); x++) {
            Node nNode = nElementsTaskList.item(x);
            String idSearched = nNode.getAttributes().getNamedItem("Id").getNodeValue();
            if (idSearched.equals(id)) {
                Node nodeName = nNode.getAttributes().getNamedItem("Name");
                if (nodeName != null) {
                    name = nodeName.getNodeValue();
                }
            }
        }
        return name;
    }

    //Cria uma arvore de valores que irão conter os valores de caminho para cada uma das atividades
    private Map<String, Map<String, Double>> createTreeOfValues() {
        Map<String, Map<String, Double>> mapOfElements = new HashMap<String, Map<String, Double>>();

        ArrayList<String> idElements = new ArrayList<String>();
        idElements = this.findAllIdElements(this.document);

        //Percorre a lista de ids para encontrar o valor de Path de cada um dos elementos
        for (int x = 0; x < idElements.size(); x++) {
            Map<String, Double> listElements = new HashMap<String, Double>();
            for (int y = 0; y < idElements.size(); y++) {
                listElements.put(idElements.get(y), 0.0);
            }
            mapOfElements.put(idElements.get(x), listElements);
        }

        return mapOfElements;

    }

    private String getAtributeValue(Node nNode, String atribute) {

        String atributeValue = "";
        if (nNode != null && nNode.hasAttributes()) {
            NamedNodeMap nodeMapAtributes = nNode.getAttributes();
            Node nAtribute = nodeMapAtributes.getNamedItem(atribute);
            if (nAtribute != null) {
                atributeValue = nAtribute.getNodeValue();
            }
        }
        return atributeValue;
    }

    private Double calculateCMPath(ArrayList<String> idsOrigem, String idDestino, Double valorInicial) {

        NodeList nElementsTransitionList = this.document.getElementsByTagName("Transition");

        for (int x = 0; x < idsOrigem.size(); x++) {
            String idOrigem = idsOrigem.get(x);
            ArrayList<String> newIdsOrigem = new ArrayList<String>();

            for (int z = 0; z < nElementsTransitionList.getLength(); z++) {
                Node nTransitionNode = nElementsTransitionList.item(z);
                //Investiga a Transição para identificar quais transições possuem a origem igual ao nó a se investigar
                String atributeFromValue = this.getAtributeValue(nTransitionNode, "From");

                if (atributeFromValue.equals(idOrigem)) {

                    String atributeToValue = this.getAtributeValue(nTransitionNode, "To");

                    valorInicial = calculateCMEdge(nTransitionNode);
                    if (atributeToValue.equals(idDestino)) {
                        cmMetricFound = true;
                        return valorInicial;
                    } else if (!newIdsOrigem.contains(atributeToValue)) {
                        //System.out.println(getNameById(idOrigem) + " to " + getNameById(atributeToValue));
                        if (this.isAndNode(atributeToValue) || this.isOrNode(atributeToValue) || this.isXorNode(atributeToValue)) {
                            newIdsOrigem.add(atributeToValue);
                        }
                    }
                }
            }
            if (newIdsOrigem.size() > 0) {
                valorInicial = valorInicial * this.calculateCMPath(newIdsOrigem, idDestino, valorInicial);
            }
        }
        if (cmMetricFound) {
            return valorInicial;
        } else {
            return 0.0;
        }
    }

    private Double calculateCMEdge(Node nNode) {

        Double value = new Double(0.0);

        Integer nodeInput = new Integer(0);
        Integer nodeOutput = new Integer(0);

        Node nId = nNode.getAttributes().getNamedItem("To");
        String id = "";
        if (nId != null) {
            id = nId.getNodeValue();
        }
        //Calcula quantas entradas e saídas há em relação ao nó
        nodeInput = this.calculateInput(id);
        nodeOutput = this.calculateOutput(id);

        value = this.calculateCMNode(id, nodeInput, nodeOutput);

        return value;
    }

    private Double calculateCMNode(String id, Integer nodeInput, Integer nodeOutput) {

        Double value = new Double(0.0);

        if (!id.equals("")) {
            if (this.isOrNode(id)) {
                double firstPartDenominator = ((Math.pow(2, nodeInput) - 1)) * (Math.pow(2, nodeOutput) - 1);
                Double firstPart = 1.00 / firstPartDenominator;

                double secondPartNumerator = ((Math.pow(2, nodeInput) - 1)) * (Math.pow(2, nodeOutput) - 1) - 1;
                double secondPartDenominator = ((Math.pow(2, nodeInput) - 1)) * (Math.pow(2, nodeOutput) - 1);
                Double secondPart = secondPartNumerator / secondPartDenominator;

                Double thirdPart = 1.00 / (nodeInput * nodeOutput);

                value = firstPart + secondPart * thirdPart;

            } else if (this.isAndNode(id)) {
                value = 1.00;
            } else if (isXorNode(id)) {
                value = 1.00 / (nodeInput * nodeOutput);
            } else {
                value = 1.00;
            }
        }

        return value;

    }

    //TODO - Deve-se verificar se o calculo está considerando que o maior caminho que deve ser considerado. Isso pode ser solucinado com uma array de double, valor final a ser retornado
    //, que guarde os valores para cada origem e destino
    //TODO - fazer com que se calcule logo neste método o maior valor para o caminho. Desta forma se evita manutenção do código em momento anterior
    @SuppressWarnings("finally")
    private Double newCalculateCCPath(NodeList nodeListAInvestigar, Double initialValue, HashMap<String, Double> listOrigem, String idDestino,
             HashMap<String, String> listNodesCovered, HashMap<String, HashMap<String, Double>> hashFinal) {

        //double valorFinal = initialValue;
        HashMap<String, HashMap<String, Double>> hashAllPaths = new HashMap<String, HashMap<String, Double>>();
        hashAllPaths = this.calculateCCAllPaths(nodeListAInvestigar, initialValue, listOrigem, idDestino, listNodesCovered, hashFinal);

        if (hashAllPaths.isEmpty()) {
            return 0.0;
        }

        //Percorre o HashMap para achar o maior valor
        Double greatestValue = 0.0;
        for (Entry<String, HashMap<String, Double>> entryMapDestino : hashAllPaths.entrySet()) {
            String idDestinoFinal = entryMapDestino.getKey();
            HashMap<String, Double> origens = entryMapDestino.getValue();

            String nomeDestinoString = this.getNameById(idDestinoFinal);
            for (Entry<String, Double> entryMapDestino2 : origens.entrySet()) {
                String nomeOrigem = this.getNameById(entryMapDestino2.getKey());
                Double valor = entryMapDestino2.getValue();
                //valorFinal = valorFinal * valor;
                if (valor > greatestValue) {
                    greatestValue = valor;
                }
            }

        }
        return greatestValue;
    }

    //Retorna o HASH com todos os caminhos de origem para destino
    private HashMap<String, HashMap<String, Double>> calculateCCAllPaths(NodeList nodeListAInvestigar, Double initialValue, HashMap<String, Double> listOrigem, String idDestino,
             HashMap<String, String> listNodesCovered, HashMap<String, HashMap<String, Double>> hashFinal) {

        Iterator it = listOrigem.entrySet().iterator();

        Double value = new Double(initialValue.doubleValue());

        //Este array corresponderá ao valor que será investigado na proxima iteração, caso não se encontre
        HashMap<String, Double> newListOrigem = new HashMap<String, Double>();
        this.ccMetricFound = false;

        while (it.hasNext() && !this.ccMetricFound) { // && !this.ccMetricFound

            Map.Entry pair = (Map.Entry) it.next();

            String idOrigem = pair.getKey().toString();
            if (idOrigem.equals(idDestino)) {
                //Returns an empty Hash which indicates the value is 0
                return new HashMap<String, HashMap<String, Double>>();
            }

            for (int x = 0; x < nodeListAInvestigar.getLength(); x++) { //
                Node nNode = nodeListAInvestigar.item(x);

                if (nNode != null && nNode.hasAttributes() && !this.ccMetricFound) {
                    NamedNodeMap nodeMapAtributes = nNode.getAttributes();
                    Node atributeFrom = nodeMapAtributes.getNamedItem("From");
                    Node atributeTo = nodeMapAtributes.getNamedItem("To");
                    if (atributeFrom != null && atributeTo != null) {
                        String atributeFromValue = atributeFrom.getNodeValue();
                        String atributeToValue = atributeTo.getNodeValue();

                        //TODO - apagar variaveis de debug
                        String sAtributeFromValue = this.getNameById(atributeFromValue);
                        String sAtributeToValue = this.getNameById(atributeToValue);

                        String sIdOrigem = this.getNameById(idOrigem);
                        String sIdDestino = this.getNameById(idDestino);

                        //Evita o loop
                        if (!listNodesCovered.containsKey(atributeFromValue) || !listNodesCovered.containsValue(atributeToValue)) {

                            if (atributeFromValue.equals(idOrigem)) {

                                //Deve-se calcular o valor da associação aqui, já que se o idOrigem é igual, então há uma associação entre ele e um próximo
                                //nó que deverá ser investigado
                                Double valorNo = (Double) pair.getValue();
                                value = valorNo * this.calculateCCEdge(nNode);

                                //Caso exista uma associação direta entre a origem e o destino passados como parâmetro então se achou uma relação
                                if (atributeToValue.equals(idDestino)) {
                                    //newListOrigem.put(atributeToValue,value);

                                    //Resgata o hash já inserido para a chave destino e então insere o novo valor descoberto 
                                    HashMap<String, Double> hashNodesDestino = hashFinal.get(idDestino);
                                    if (hashNodesDestino == null) {
                                        hashNodesDestino = new HashMap<String, Double>();
                                        hashNodesDestino.put(atributeFromValue, value);
                                    } else {
                                        //Insere no hasDestino apenas o maior valor atribuido entre a ultima origem e o destino final já que este caminho pode ter sido percorrido mais de uma vez
                                        // 1 -> 4 -> 7 = 1.0 ;  1-> 7 = 0.1111
                                        if (hashNodesDestino.containsKey(atributeFromValue)) {
                                            Double valorAtualAtributeFromValueToDestino = hashNodesDestino.get(atributeFromValue);
                                            if (value > valorAtualAtributeFromValueToDestino) {
                                                hashNodesDestino.put(atributeFromValue, value);
                                            }
                                        }else{
                                            hashNodesDestino.put(atributeFromValue, value);
                                        }
                                    }

                                    if (this.notExistedOrisGreater(hashFinal, idDestino, atributeFromValue, value)) {
                                        hashFinal.put(idDestino, hashNodesDestino);
                                    }
                                    //Caso o valor seja 1, que é o maior valor possível a ser encontrado, então a aplicação retorna logo o HashFinal para evitar futuros loops
                                    if (value == 1) {
                                        this.ccMetricFound = true;
                                    }

                                    //newListOrigem.remove(atributeToValue);
                                } else {
                                    //Caso só a origem seja igual ao parâmetro passado então é necessário investigar os proximos nós ligados à origem 
                                    //lista para evitar loop
                                    listNodesCovered.put(atributeFromValue, atributeToValue);
                                    newListOrigem.put(atributeToValue, value);
                                    value = 1.0;
                                }
                            }
                        }
                    }
                }
            }
            //Caso tenha percorrido todo a árvore de dados, então se deve pesquisar pelas ligações da origem
            if (newListOrigem.size() > 0) {
                //TODO - deve-se incrementar o hash e não igualar
                HashMap<String, HashMap<String, Double>> hashFound = new HashMap<String, HashMap<String, Double>>();
                hashFound = this.calculateCCAllPaths(nodeListAInvestigar, value, newListOrigem, idDestino, listNodesCovered, hashFinal);

                //hashFinal = this.insertIfGreater(hashFinal,hashFound);
                hashFinal.putAll(hashFound);
                //hashFinal;
            }
        }
        return hashFinal;
    }

    private Boolean notExistedOrisGreater(HashMap<String, HashMap<String, Double>> hashToBeCompared, String idDestinoToBeInserted, String atributeFromValueToBeInserted, Double valueToBeInserted) {

        Boolean bNotExistedOrGreater = true;

        HashMap<String, HashMap<String, Double>> hashFinal = new HashMap<String, HashMap<String, Double>>();
        hashFinal = hashToBeCompared;

        if (hashToBeCompared.containsKey(idDestinoToBeInserted)) {

            HashMap<String, Double> hashOrignValueToBeCompared = hashToBeCompared.get(idDestinoToBeInserted);
            if (hashOrignValueToBeCompared.containsKey(atributeFromValueToBeInserted)) {
                Double valueToBeCompared = hashOrignValueToBeCompared.get(atributeFromValueToBeInserted);
                if (valueToBeInserted < valueToBeCompared) {
                    bNotExistedOrGreater = false;
                }

            }

        }

        return bNotExistedOrGreater;

    }

    private HashMap<String, HashMap<String, Double>> insertIfGreater(HashMap<String, HashMap<String, Double>> hashToBeCompared, HashMap<String, HashMap<String, Double>> hashToBeInserted) {

        HashMap<String, HashMap<String, Double>> hashFinal = new HashMap<String, HashMap<String, Double>>();
        hashFinal = hashToBeCompared;

        Iterator itHashToBeInserted = hashToBeInserted.entrySet().iterator();

        while (itHashToBeInserted.hasNext()) {

            Map.Entry pairToBeInserted = (Map.Entry) itHashToBeInserted.next();

            String idDestinoToBeInserted = pairToBeInserted.getKey().toString();

            if (hashToBeCompared.containsKey(idDestinoToBeInserted)) {
                //Pesquisa no hash que será comparado se existe a origem que será inserida
                HashMap<String, Double> origemValorFinalToBeCompared = hashToBeCompared.get(idDestinoToBeInserted);

                HashMap<String, Double> hashIdOrigemValorToBeInserted = (HashMap<String, Double>) pairToBeInserted.getValue();

                Iterator itHashOrigemValueToBeInserted = hashIdOrigemValorToBeInserted.entrySet().iterator();
                while (itHashOrigemValueToBeInserted.hasNext()) {

                    Map.Entry pairOrigemValueToBeInserted = (Map.Entry) itHashOrigemValueToBeInserted.next();
                    String idOrigemToBeInserted = pairOrigemValueToBeInserted.getKey().toString();
                    if (origemValorFinalToBeCompared.containsKey(idOrigemToBeInserted)) {
                        Double valorCompared = origemValorFinalToBeCompared.get(idOrigemToBeInserted);
                        Double valorToBeInserted = hashIdOrigemValorToBeInserted.get(idOrigemToBeInserted);
                        if (valorCompared < valorToBeInserted) {
                            hashFinal.put(idDestinoToBeInserted, hashIdOrigemValorToBeInserted);
                        }
                    }

                }
            }

        }
        return hashFinal;

    }

    //TODO - Deve-se verificar se o calculo está considerando que o maior caminho que deve ser considerado. Isso pode ser solucinado com uma array de double, valor final a ser retornado
    //, que guarde os valores para cada origem e destino
    @SuppressWarnings("finally")
    private Double calculateCCPath(NodeList nodeListAInvestigar, Double initialValue, HashMap<String, Double> listOrigem, String idDestino,
             HashMap<String, String> listNodesCovered) {

        Double value = new Double(initialValue.doubleValue());

        //Este array corresponderá ao valor que será investigado na proxima iteração, caso não se encontre
        HashMap<String, Double> newListOrigem = new HashMap<String, Double>();

        //TODO - retirar o try - catch
        try {
            Iterator it = listOrigem.entrySet().iterator();

            while (it.hasNext()) {

                Map.Entry pair = (Map.Entry) it.next();

                String idOrigem = pair.getKey().toString();
                if (idOrigem.equals(idDestino)) {
                    return 0.00;
                }

                for (int x = 0; x < nodeListAInvestigar.getLength(); x++) {
                    Node nNode = nodeListAInvestigar.item(x);

                    if (nNode != null && nNode.hasAttributes()) {
                        NamedNodeMap nodeMapAtributes = nNode.getAttributes();
                        Node atributeFrom = nodeMapAtributes.getNamedItem("From");
                        Node atributeTo = nodeMapAtributes.getNamedItem("To");
                        if (atributeFrom != null && atributeTo != null) {
                            String atributeFromValue = atributeFrom.getNodeValue();
                            String atributeToValue = atributeTo.getNodeValue();
                            //Evita o loop
                            if (!listNodesCovered.containsKey(atributeFromValue) || !listNodesCovered.containsValue(atributeToValue)) {

                                if (atributeFromValue.equals(idOrigem)) {

                                    //Deve-se calcular o valor da associação aqui, já que se o idOrigem é igual, então há uma associação entre ele e um próximo
                                    //nó que deverá ser investigado
                                    Double valorNo = (Double) pair.getValue();
                                    value = valorNo * this.calculateCCEdge(nNode);

                                    //Caso exista uma associação direta entre a origem e o destino passados como parâmetro então se achou uma relação
                                    if (atributeToValue.equals(idDestino)) {
                                        this.ccMetricFound = true;
                                        return value;
                                        //Caso só a origem seja igual ao parâmetro passado então é necessário investigar os proximos nós ligados à origem 
                                    } else {
                                        //lista para evitar loop
                                        listNodesCovered.put(atributeFromValue, atributeToValue);

                                        newListOrigem.put(atributeToValue, value);
                                        value = 1.0;
                                    }
                                }
                            }
                        }
                    }
                }
                //Caso tenha percorrido todo a árvore de dados, então se deve pesquisar pelas ligações da origem
                if (newListOrigem.size() > 0) {
                    value = initialValue * this.calculateCCPath(nodeListAInvestigar, value, newListOrigem, idDestino, listNodesCovered);
                }
            }
        } catch (Exception e) {
            System.out.println("Erro");
        } finally {
            if (this.ccMetricFound == false) {
                return 0.0;
            } else {
                return value;
            }
        }
    }

    //Este método calcula o degree de um nó, ou seja, calcula quantas entradas e saídas há relacionas ao nó
    private Integer calculateCCNodeDegree(String nNodeIdValue) {
        Integer degree = 0;

        NodeList nElementsTaskList = this.document.getElementsByTagName("Transition");

        for (int x = 0; x < nElementsTaskList.getLength(); x++) {
            String from = "";
            String to = "";

            Node nInternalNode = nElementsTaskList.item(x);
            Node nodeFrom = nInternalNode.getAttributes().getNamedItem("From");
            if (nodeFrom != null) {
                from = nodeFrom.getNodeValue();
            }
            Node nodeTo = nInternalNode.getAttributes().getNamedItem("To");
            if (nodeTo != null) {
                to = nodeTo.getNodeValue();
            }

            if (from.equals(nNodeIdValue) || to.equals(nNodeIdValue)) {
                degree++;
            }
        }
        return degree;
    }

    private Double calculateCCEdge(Node nNode) {

        Double valueFrom = new Double(0.0);
        Double valueTo = new Double(0.0);

        Integer nodeDegreeFrom = new Integer(0);
        Integer nodeDegreeTo = new Integer(0);

        Node nodeFrom = nNode.getAttributes().getNamedItem("From");
        String idFrom = "";
        if (nodeFrom != null) {
            idFrom = nodeFrom.getNodeValue();
        }
        //Calcula quantas saídas e entradas há em relação ao nó
        nodeDegreeFrom = this.calculateCCNodeDegree(idFrom);

        Node nodeTo = nNode.getAttributes().getNamedItem("To");
        String idTo = "";
        if (nodeTo != null) {
            idTo = nodeTo.getNodeValue();
        }
        //Calcula quantas saídas e entradas há em relação ao nó
        nodeDegreeTo = this.calculateCCNodeDegree(idTo);

        valueFrom = this.calculateCCNode(idFrom, nodeDegreeFrom);
        valueTo = this.calculateCCNode(idTo, nodeDegreeTo);

        return valueFrom * valueTo;
    }

    private Double calculateCCNode(String id, Integer nodeDegree) {

        Double value = new Double(0.0);

        if (!id.equals("")) {
            if (this.isOrNode(id)) {
                Double firstPart = 1.00 / ((Math.pow(2, nodeDegree) - 1));
                Double secondPart = (Math.pow(2, nodeDegree) - 2) / (Math.pow(2, nodeDegree) - 1);
                Double thirdPart = 1.00 / nodeDegree;

                value = firstPart + secondPart * thirdPart;

            } else if (this.isAndNode(id)) {
                value = 1.00;
            } else if (isXorNode(id)) {
                value = 1.00 / nodeDegree;
            } else {
                value = 1.00;
            }
        }
        return value;
    }

    private Boolean isOrNode(String id) {

        if (this.findNodesWithOR(this.document).contains(id)) {
            return true;
        }

        return false;
    }

    private Boolean isAndNode(String id) {

        if (this.findNodesWithAND(this.document).contains(id)) {
            return true;
        }

        return false;
    }

    private Boolean isXorNode(String id) {

        if (this.findNodesWithXOR(this.document).contains(id)) {
            return true;
        }

        return false;
    }

    private Boolean isOrTransition(Node nNode) {

        Node nodeFrom = nNode.getAttributes().getNamedItem("From");
        if (nodeFrom != null) {
            String idFrom = nodeFrom.getNodeValue();
            if (this.findNodesWithOR(this.document).contains(idFrom)) {
                return true;
            }
        }

        return false;
    }

    private Boolean isAndTransition(Node nNode) {

        Node nodeFrom = nNode.getAttributes().getNamedItem("From");
        if (nodeFrom != null) {
            String idFrom = nodeFrom.getNodeValue();
            if (this.findNodesWithAND(this.document).contains(idFrom)) {
                return true;
            }
        }

        return false;
    }

    private Boolean isXorTransition(Node nNode) {

        Node nodeFrom = nNode.getAttributes().getNamedItem("From");
        if (nodeFrom != null) {
            String idFrom = nodeFrom.getNodeValue();
            if (this.findNodesWithXOR(this.document).contains(idFrom)) {
                return true;
            }
        }

        return false;
    }

    private ArrayList<String> findOnlyIdActivitiesElements(Document doc) {

        ArrayList<String> elements = new ArrayList<String>();

        NodeList nElementsTaskList = doc.getElementsByTagName("Activity");

        for (int x = 0; x < nElementsTaskList.getLength(); x++) {
            Node nNode = nElementsTaskList.item(x);
            String id = nNode.getAttributes().getNamedItem("Id").getNodeValue();
            if (!isOrNode(id) && !isXorNode(id) && !isAndNode(id)) {
                elements.add(id);
            }
        }

        return elements;

    }

    private ArrayList<String> findAllIdElements(Document doc) {

        ArrayList<String> elements = new ArrayList<String>();

        NodeList nElementsTaskList = doc.getElementsByTagName("Activity");

        for (int x = 0; x < nElementsTaskList.getLength(); x++) {
            Node nNode = nElementsTaskList.item(x);
            String id = nNode.getAttributes().getNamedItem("Id").getNodeValue();
            elements.add(id);
        }

        return elements;
    }

    private ArrayList<String> findNodesWithOR(Document doc) {
        NodeList nList = doc.getElementsByTagName("Activity");
        ArrayList<String> ids = new ArrayList<String>();

        for (int x = 0; x < nList.getLength(); x++) {
            Node nNode = nList.item(x);

            if (nNode.hasChildNodes()) {
                NodeList subNodeList = nNode.getChildNodes();

                for (int y = 0; y < subNodeList.getLength(); y++) {
                    Node nSubNode = subNodeList.item(y);

                    if (nSubNode.getNodeName().equals("Route")) {
                        //A atividade representa um OR caso esteja marcado como GatewayType=Inclusive ou esteja vazio - não possui atributos (else) 
                        if (nSubNode.hasAttributes()) {
                            for (int z = 0; z < nSubNode.getAttributes().getLength(); z++) {
                                Node nNodeAtribute = nSubNode.getAttributes().item(z);
                                if (nNodeAtribute.getNodeName().equals("GatewayType") && nNodeAtribute.getNodeValue().equals("Inclusive")) {
                                    ids.add(nSubNode.getParentNode().getAttributes().getNamedItem("Id").getNodeValue());
                                }
                            }
                        } else {
                            ids.add(nSubNode.getParentNode().getAttributes().getNamedItem("Id").getNodeValue());
                        }
                    }
                }
            }

        }
        return ids;
    }

    private ArrayList<Node> findTransitionsById(String idOrigem) {
        NodeList nList = this.document.getElementsByTagName("Transition");
        ArrayList<Node> aNodes = new ArrayList<Node>();

        for (int x = 0; x < nList.getLength(); x++) {
            Node nNode = nList.item(x);
            if (nNode.hasAttributes()) {
                NamedNodeMap nodeMap = nNode.getAttributes();
                Node nodeFrom = nodeMap.getNamedItem("From");
                if (nodeFrom != null) {
                    String atributeFromValue = nodeFrom.getNodeValue();
                    if (atributeFromValue.equals(idOrigem)) {
                        aNodes.add(nNode);
                    }
                }
            }
        }
        return aNodes;
    }

    private ArrayList<String> findNodesWithAND(Document doc) {
        NodeList nList = doc.getElementsByTagName("Activity");
        ArrayList<String> ids = new ArrayList<String>();

        for (int x = 0; x < nList.getLength(); x++) {
            Node nNode = nList.item(x);

            if (nNode.hasChildNodes()) {
                NodeList subNodeList = nNode.getChildNodes();

                for (int y = 0; y < subNodeList.getLength(); y++) {
                    Node nSubNode = subNodeList.item(y);
                    if (nSubNode.hasAttributes()) {
                        NamedNodeMap nodeMap = nSubNode.getAttributes();
                        Node nodeType = nodeMap.getNamedItem("GatewayType");
                        if (nodeType != null && nodeType.getNodeValue().equals("Parallel")) {
                            ids.add(nSubNode.getParentNode().getAttributes().getNamedItem("Id").getNodeValue());
                        }
                    }

                }
            }

        }
        return ids;
    }

    private ArrayList<String> findNodesWithXOR(Document doc) {
        NodeList nList = doc.getElementsByTagName("Activity");
        ArrayList<String> ids = new ArrayList<String>();

        for (int x = 0; x < nList.getLength(); x++) {
            Node nNode = nList.item(x);

            if (nNode.hasChildNodes()) {
                NodeList subNodeList = nNode.getChildNodes();

                for (int y = 0; y < subNodeList.getLength(); y++) {
                    Node nSubNode = subNodeList.item(y);
                    if (nSubNode.hasAttributes()) {
                        NamedNodeMap nodeMap = nSubNode.getAttributes();
                        Node nodeType = nodeMap.getNamedItem("MarkerVisible");
                        if (nodeType != null && nodeType.getNodeValue().equals("true")) {
                            ids.add(nSubNode.getParentNode().getAttributes().getNamedItem("Id").getNodeValue());
                        }
                    }

                }
            }

        }
        return ids;
    }

    private int calculateInput(String idOrigem) {
        int input = 0;
        NodeList nList = this.document.getElementsByTagName("Transition");

        for (int x = 0; x < nList.getLength(); x++) {

            Node nNode = nList.item(x);
            String atributeTo = this.getAtributeValue(nNode, "To");
            if (atributeTo.equals(idOrigem)) {
                input++;
            }
        }

        return input;
    }

    private int calculateOutput(String idOrigem) {
        int output = 0;
        NodeList nList = this.document.getElementsByTagName("Transition");

        for (int x = 0; x < nList.getLength(); x++) {

            Node nNode = nList.item(x);
            String atributeTo = this.getAtributeValue(nNode, "From");
            if (atributeTo.equals(idOrigem)) {
                output++;
            }
        }

        return output;
    }

    private int calculateInputOutPut(Document doc, ArrayList<String> idsNodes) {
        int outPut = 0;
        int inPut = 0;

        NodeList nList = doc.getElementsByTagName("Transition");
        for (int x = 0; x < nList.getLength(); x++) {
            Node nNode = nList.item(x);
            if (nNode.hasAttributes()) {
                NamedNodeMap nodeMap = nNode.getAttributes();
                Node nodeFrom = nodeMap.getNamedItem("From");
                if (nodeFrom != null) {
                    if (idsNodes.contains(nodeFrom.getNodeValue())) {
                        outPut++;
                    }

                }
                Node nodeTo = nodeMap.getNamedItem("To");
                if (nodeTo != null) {
                    if (idsNodes.contains(nodeTo.getNodeValue())) {
                        inPut++;
                    }

                }
            }
        }
        return outPut * inPut;
    }

    private int calculateOutPut(ArrayList<String> idNodes) {
        int outPutOr = 0;
        NodeList nList = this.document.getElementsByTagName("Transition");
        for (int x = 0; x < nList.getLength(); x++) {
            Node nNode = nList.item(x);
            if (nNode.hasAttributes()) {
                NamedNodeMap nodeMap = nNode.getAttributes();
                Node nodeFrom = nodeMap.getNamedItem("From");
                if (nodeFrom != null) {
                    if (idNodes.contains(nodeFrom.getNodeValue())) {
                        outPutOr++;
                    }

                }
            }
        }
        return outPutOr;
    }
}
