package com.unieuro.agents;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.Description;

@Agent
@Description("Agente que joga o Campo Minado automaticamente com heuristicas basicas.")
public class JogadorAgent {

    private int[][] campo;          // 0 = vazio, 1 = mina
    private int[][] adjacencias;    // -1 = mina, 0–8 = minas ao redor
    private boolean[][] revelado;
    private boolean[][] marcada;
    private int tamanho = 9;
    private Random random = new Random();
    private int totalMines = 0; // total real de minas (este exemplo carrega o campo do arquivo)

    // controla quantas bandeiras/flags ainda podem ser usadas
    private int flagsRemaining = 0;

    @AgentBody
    public void execute() {
        carregarCampo();
        carregarAdjacencias();
        // inicializa arquivo de minas encontradas (caso UI utilize)
        atualizarArquivoMinas();
        jogar();
    }

    private void carregarCampo() {
        try (BufferedReader br = new BufferedReader(new FileReader("campo.csv"))) {
            List<int[]> linhas = new ArrayList<>();
            String linha;
            while ((linha = br.readLine()) != null) {
                linha = linha.trim();
                if (linha.isEmpty()) continue;
                String[] partes = linha.split("\\s*,\\s*");
                int[] nums = new int[partes.length];
                for (int i = 0; i < partes.length; i++) {
                    nums[i] = Integer.parseInt(partes[i]);
                }
                linhas.add(nums);
            }
            campo = linhas.toArray(new int[0][]);
            tamanho = campo.length;
            revelado = new boolean[tamanho][tamanho];
            marcada = new boolean[tamanho][tamanho];
            // calcula total de minas
            totalMines = 0;
            for (int i = 0; i < tamanho; i++) {
                for (int j = 0; j < tamanho; j++) {
                    if (campo[i][j] == 1) totalMines++;
                }
            }
            // inicializa flagsRemaining com o total real
            flagsRemaining = totalMines;
            System.out.println("[JogadorAgent] Campo carregado. Total minas = " + totalMines + ". Comecando a jogar...\n");
        } catch (IOException e) {
            System.err.println("Erro ao carregar campo: " + e.getMessage());
        }
    }

    private void carregarAdjacencias() {
        try (BufferedReader br = new BufferedReader(new FileReader("adjacencias.csv"))) {
            List<int[]> linhas = new ArrayList<>();
            String linha;
            while ((linha = br.readLine()) != null) {
                linha = linha.trim();
                if (linha.isEmpty()) continue;
                String[] partes = linha.split("\\s*,\\s*");
                int[] nums = new int[partes.length];
                for (int i = 0; i < partes.length; i++) {
                    nums[i] = Integer.parseInt(partes[i]);
                }
                linhas.add(nums);
            }
            adjacencias = linhas.toArray(new int[0][]);
        } catch (IOException e) {
            System.err.println("Erro ao carregar adjacencias: " + e.getMessage());
        }
    }

    private void jogar() {
        int jogada = 1;
        boolean progresso;

        // primeira jogada segura (prioriza abrir uma regiao com adjacencias==0)
        realizarPrimeiraJogadaSegura(jogada++);
        imprimirCampo();
        imprimirTempoAtual();

        while (true) {
            // Aplica regras deterministicas/sensores primeiro
            progresso = aplicarSensoresDeterministicos();

            if (!progresso) {
                // tenta heuristica exata (enumeracao)
                progresso = aplicarHeuristicas();
            }

            if (!progresso) {
                // se nao houver jogadas logicas, faz uma jogada probabilistica (menor probabilidade)
                realizarJogadaProbabilistica(jogada++);
            }

            // imprime campo e tempo apos a iteracao principal (caso alguma acao tenha ocorrido já os prints ja terao sido executados)
            imprimirCampo();
            imprimirTempoAtual();

            if (verificarVitoria()) {
                System.out.println("\nParabens! Todas as celulas seguras foram reveladas!");
                escreverResultado("VENCEU");
                break;
            }

            try {
                Thread.sleep(1200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Regras deterministicas classicas:
     * 1) Se (valor - marcadasAdj) == numeroVizinhasNaoReveladas  => marcar todas as vizinhas nao reveladas
     * 2) Se (valor == marcadasAdj) => revelar todas as vizinhas nao reveladas (sao seguras)
     * Retorna true se houve alguma acao (marcar ou revelar).
     */
    private boolean aplicarSensoresDeterministicos() {
        boolean mudou = false;

        // percorre todas as celulas reveladas com numero
        for (int x = 0; x < tamanho; x++) {
            for (int y = 0; y < tamanho; y++) {
                if (!revelado[x][y]) continue;
                int val = adjacencias[x][y];
                if (val <= 0) continue;

                List<int[]> vizinhas = obterVizinhas(x, y);
                int marc = 0;
                int naoRev = 0;
                List<int[]> desconhecidas = new ArrayList<>();
                for (int[] v : vizinhas) {
                    int vx = v[0], vy = v[1];
                    if (marcada[vx][vy]) marc++;
                    else if (!revelado[vx][vy]) { naoRev++; desconhecidas.add(new int[]{vx, vy}); }
                }

                int bombasFaltando = val - marc;
                // Regra 1: todas as desconhecidas sao minas
                if (bombasFaltando > 0 && bombasFaltando == naoRev) {
                    for (int[] c : desconhecidas) {
                        int cx = c[0], cy = c[1];
                        if (!marcada[cx][cy]) {
                            if (flagsRemaining <= 0) {
                                // sem bandeiras restantes -> nao marcar
                                System.out.println("[Sensor] Sem bandeiras restantes, nao e possivel marcar (" + (cx+1) + "," + (cy+1) + ")");
                                continue;
                            }
                            marcada[cx][cy] = true;
                            flagsRemaining--;
                            System.out.printf("[Sensor] Marcando (%d,%d) como MINA. Bandeiras restantes: %02d%n", cx + 1, cy + 1, flagsRemaining);
                            atualizarArquivoMinas();
                            imprimirCampo();
                            imprimirTempoAtual();
                            mudou = true;
                        }
                    }
                }
                // Regra 2: se ja marcou todas as minas ao redor, revela as demais vizinhas desconhecidas
                else if (bombasFaltando <= 0 && !desconhecidas.isEmpty()) {
                    for (int[] c : desconhecidas) {
                        int cx = c[0], cy = c[1];
                        revelarCelula(cx, cy);
                        // revelarCelula ja imprime o campo e tempo
                        mudou = true;
                    }
                }
            }
        }

        return mudou;
    }

    /**
     * Calcula a probabilidade aproximada de cada cela (nao revelada e nao marcada) conter uma mina.
     * Retorna uma matriz double com valores entre 0.0 e 1.0.
     */
    private double[][] calcularProbabilidades() {
        double[][] prob = new double[tamanho][tamanho];

        // Inicializa probabilidades com estimativa global (restantes minas / celulas nao reveladas)
        int naoReveladas = 0;
        int marcadasCount = 0;
        for (int i = 0; i < tamanho; i++) {
            for (int j = 0; j < tamanho; j++) {
                if (!revelado[i][j] && !marcada[i][j]) naoReveladas++;
                if (marcada[i][j]) marcadasCount++;
            }
        }
        int minasRestantes = Math.max(0, totalMines - marcadasCount);
        double globalProb = naoReveladas > 0 ? ((double) minasRestantes) / naoReveladas : 1.0;

        for (int i = 0; i < tamanho; i++) {
            for (int j = 0; j < tamanho; j++) {
                prob[i][j] = 0.0;
                if (revelado[i][j] || marcada[i][j]) {
                    prob[i][j] = 1.0; // tratado como nao clicavel
                    continue;
                }
                // start with global prior
                prob[i][j] = globalProb;
            }
        }

        // Refinamento local: para cada cela revelada com numero, distribuimos a probabilidade entre vizinhas nao reveladas
        for (int x = 0; x < tamanho; x++) {
            for (int y = 0; y < tamanho; y++) {
                if (!revelado[x][y]) continue;
                int valor = adjacencias[x][y];
                if (valor <= 0) continue;

                List<int[]> vizinhas = obterVizinhas(x, y);
                int naoRev = 0;
                int marc = 0;
                List<int[]> candidatos = new ArrayList<>();
                for (int[] v : vizinhas) {
                    int vx = v[0], vy = v[1];
                    if (marcada[vx][vy]) marc++;
                    else if (!revelado[vx][vy]) { naoRev++; candidatos.add(new int[]{vx, vy}); }
                }

                int bombasFaltando = valor - marc;
                if (bombasFaltando <= 0 || candidatos.isEmpty()) continue;

                double p = ((double) bombasFaltando) / candidatos.size();
                for (int[] c : candidatos) {
                    int cx = c[0], cy = c[1];
                    double prior = prob[cx][cy];
                    double local = p;
                    double combined = 1.0 - (1.0 - prior) * (1.0 - local);
                    prob[cx][cy] = Math.min(1.0, combined);
                }
            }
        }

        // Tenta refinar usando enumeracao exata na fronteira (quando pequeno o suficiente)
        int marcadasTotal = 0;
        for (int i = 0; i < tamanho; i++) for (int j = 0; j < tamanho; j++) if (marcada[i][j]) marcadasTotal++;
        int minasRestantes2 = Math.max(0, totalMines - marcadasTotal);
        double[][] exact = calcularProbabilidadesExatas(minasRestantes2);
        if (exact != null) {
            for (int i = 0; i < tamanho; i++) {
                for (int j = 0; j < tamanho; j++) {
                    if (exact[i][j] >= 0.0) prob[i][j] = exact[i][j];
                }
            }
        }

        return prob;
    }

    private double[][] calcularProbabilidadesExatas(int minasRestantes) {
        boolean[][] isFrontier = new boolean[tamanho][tamanho];
        List<int[]> frontierList = new ArrayList<>();
        int outsideUnknowns = 0;
        for (int i = 0; i < tamanho; i++) {
            for (int j = 0; j < tamanho; j++) {
                if (revelado[i][j]) continue;
                if (marcada[i][j]) continue;
                boolean adjNumber = false;
                for (int[] v : obterVizinhas(i, j)) {
                    if (revelado[v[0]][v[1]] && adjacencias[v[0]][v[1]] > 0) { adjNumber = true; break; }
                }
                if (adjNumber) { isFrontier[i][j] = true; frontierList.add(new int[]{i, j}); }
                else outsideUnknowns++;
            }
        }

        int n = frontierList.size();
        if (n == 0) return null;
        if (n > 18) return null;

        class Constraint { int[] indices; int bombs; }
        List<Constraint> constraints = new ArrayList<>();
        for (int x = 0; x < tamanho; x++) {
            for (int y = 0; y < tamanho; y++) {
                if (!revelado[x][y]) continue;
                int val = adjacencias[x][y];
                if (val <= 0) continue;
                List<Integer> idxs = new ArrayList<>();
                int marked = 0;
                for (int[] v : obterVizinhas(x, y)) {
                    int vx = v[0], vy = v[1];
                    if (marcada[vx][vy]) marked++;
                    else if (isFrontier[vx][vy]) {
                        for (int k = 0; k < frontierList.size(); k++) {
                            int[] f = frontierList.get(k);
                            if (f[0] == vx && f[1] == vy) { idxs.add(k); break; }
                        }
                    }
                }
                if (!idxs.isEmpty()) {
                    Constraint c = new Constraint();
                    c.indices = idxs.stream().mapToInt(Integer::intValue).toArray();
                    c.bombs = val - marked;
                    constraints.add(c);
                }
            }
        }

        double totalWeightCount = 0.0;
        double[] mineCounts = new double[n];

        int maxMask = 1 << n;
        for (int mask = 0; mask < maxMask; mask++) {
            int minesInFrontier = Integer.bitCount(mask);
            int remainingMines = minasRestantes - minesInFrontier;
            if (remainingMines < 0 || remainingMines > outsideUnknowns) continue;

            boolean ok = true;
            for (Constraint c : constraints) {
                int s = 0;
                for (int idx : c.indices) {
                    if (((mask >> idx) & 1) != 0) s++;
                }
                if (s != c.bombs) { ok = false; break; }
            }
            if (!ok) continue;

            double weight = nCrDouble(outsideUnknowns, remainingMines);
            totalWeightCount += weight;
            for (int i = 0; i < n; i++) {
                if (((mask >> i) & 1) != 0) mineCounts[i] += weight;
            }
        }

        if (totalWeightCount == 0.0) return null;

        double[][] res = new double[tamanho][tamanho];
        for (int i = 0; i < tamanho; i++) for (int j = 0; j < tamanho; j++) res[i][j] = -1.0;
        for (int k = 0; k < n; k++) {
            int[] f = frontierList.get(k);
            res[f[0]][f[1]] = mineCounts[k] / totalWeightCount;
        }
        return res;
    }

    private double nCrDouble(int n, int r) {
        if (r < 0 || r > n) return 0.0;
        r = Math.min(r, n - r);
        double res = 1.0;
        for (int i = 1; i <= r; i++) {
            res *= (double) (n - r + i);
            res /= (double) i;
        }
        return res;
    }

    private boolean aplicarHeuristicas() {
        int marcadasTotal = 0;
        for (int i = 0; i < tamanho; i++) for (int j = 0; j < tamanho; j++) if (marcada[i][j]) marcadasTotal++;
        int minasRestantes = Math.max(0, totalMines - marcadasTotal);
        double[][] exact = calcularProbabilidadesExatas(minasRestantes);

        if (exact == null) {
            return false;
        }

        boolean mudouAlgo = false;
        double EPS = 1e-9;
        for (int i = 0; i < tamanho; i++) {
            for (int j = 0; j < tamanho; j++) {
                if (revelado[i][j] || marcada[i][j]) continue;
                double v = exact[i][j];
                if (v < -0.5) continue;
                if (v >= 1.0 - EPS) {
                    if (!marcada[i][j]) {
                        if (flagsRemaining > 0) {
                            marcada[i][j] = true;
                            flagsRemaining--;
                            System.out.printf("[Heuristica-Exata] Marcando (%d,%d) como MINA. Bandeiras restantes: %02d.%n", i + 1, j + 1, flagsRemaining);
                            atualizarArquivoMinas();
                            imprimirCampo();
                            imprimirTempoAtual();
                            mudouAlgo = true;
                        } else {
                            System.out.printf("[Heuristica-Exata] Deseja marcar (%d,%d) mas sem bandeiras restantes.%n", i + 1, j + 1);
                        }
                    }
                } else if (v <= EPS) {
                    revelarCelula(i, j); // revelarCelula ja imprime campo e tempo
                    mudouAlgo = true;
                }
            }
        }

        return mudouAlgo;
    }

    private void revelarCelula(int x, int y) {
        if (revelado[x][y] || marcada[x][y]) return;

        if (campo[x][y] == 1) {
            revelado[x][y] = true;
            System.out.printf("BOOM! Bateu em uma mina em (%d,%d)! Fim de jogo!%n", x + 1, y + 1);
            imprimirCampo();
            escreverResultado("PERDEU");
            System.exit(0);
        }

        revelado[x][y] = true;
        System.out.printf("[Heuristica] Revelando celula segura (%d,%d).%n", x + 1, y + 1);

        if (adjacencias[x][y] == 0) {
            revelarVizinhosBFS(x, y);
        }

        // imprime estado apos revelar esta celula (ou cascata)
        atualizarArquivoMinas();
        imprimirCampo();
        imprimirTempoAtual();
    }

    private void realizarJogadaAleatoria(int jogada) {
        while (true) {
            int x = random.nextInt(tamanho);
            int y = random.nextInt(tamanho);
            if (revelado[x][y] || marcada[x][y]) continue;

            boolean bomba = campo[x][y] == 1;
            int minasAoRedor = adjacencias[x][y];
            revelado[x][y] = true;

            System.out.printf("[JogadorAgent] Jogada %d: (%d,%d) => %s (%d minas ao redor)%n",
                    jogada, x + 1, y + 1,
                    bomba ? "BOMBA" : "seguro", minasAoRedor);

            if (bomba) {
                imprimirCampo();
                System.out.println("\nBOOM! Fim de jogo!");
                escreverResultado("PERDEU");
                System.exit(0);
            }

            if (minasAoRedor == 0) {
                revelarVizinhosBFS(x, y);
            }

            atualizarArquivoMinas();
            imprimirCampo();
            imprimirTempoAtual();
            break;
        }
    }

    private void realizarPrimeiraJogadaSegura(int jogada) {
        List<int[]> zeros = new ArrayList<>();
        List<int[]> safe = new ArrayList<>();
        for (int i = 0; i < tamanho; i++) {
            for (int j = 0; j < tamanho; j++) {
                if (campo[i][j] == 1) continue; // mina
                int val = adjacencias[i][j];
                if (val == 0) zeros.add(new int[]{i, j});
                safe.add(new int[]{i, j});
            }
        }

        int x, y;
        if (!zeros.isEmpty()) {
            int[] c = zeros.get(random.nextInt(zeros.size()));
            x = c[0]; y = c[1];
        } else if (!safe.isEmpty()) {
            int bestVal = Integer.MAX_VALUE;
            int[] best = safe.get(0);
            for (int[] s : safe) {
                int v = adjacencias[s[0]][s[1]];
                if (v >= 0 && v < bestVal) { bestVal = v; best = s; }
            }
            x = best[0]; y = best[1];
        } else {
            x = random.nextInt(tamanho); y = random.nextInt(tamanho);
        }

        System.out.printf("[JogadorAgent] Primeira jogada segura: Jogada %d: (%d,%d) => %s (%d minas ao redor)%n",
                jogada, x + 1, y + 1,
                (campo[x][y] == 1 ? "BOMBA" : "seguro"),
                adjacencias[x][y]);

        if (campo[x][y] == 1) {
            System.out.println("Primeira jogada caiu em mina inesperada — abortando.");
            System.exit(0);
        }

        revelado[x][y] = true;
        if (adjacencias[x][y] == 0) {
            revelarVizinhosBFS(x, y);
        }

        atualizarArquivoMinas();
        imprimirCampo();
        imprimirTempoAtual();
    }

    private List<int[]> obterVizinhas(int x, int y) {
        List<int[]> vizinhas = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                int nx = x + dx, ny = y + dy;
                if (nx >= 0 && ny >= 0 && nx < tamanho && ny < tamanho) {
                    vizinhas.add(new int[]{nx, ny});
                }
            }
        }
        return vizinhas;
    }

    private void revelarVizinhosBFS(int startX, int startY) {
        Queue<int[]> queue = new LinkedList<>();
        if (!revelado[startX][startY]) {
            revelado[startX][startY] = true;
        }
        queue.add(new int[]{startX, startY});

        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            int x = cur[0];
            int y = cur[1];

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) continue;
                    int nx = x + dx;
                    int ny = y + dy;
                    if (nx >= 0 && ny >= 0 && nx < tamanho && ny < tamanho && !revelado[nx][ny]) {
                        revelado[nx][ny] = true;
                        if (adjacencias[nx][ny] == 0) {
                            queue.add(new int[]{nx, ny});
                        }
                    }
                }
            }
        }
    }

    private boolean verificarVitoria() {
        for (int i = 0; i < tamanho; i++) {
            for (int j = 0; j < tamanho; j++) {
                if (!revelado[i][j] && campo[i][j] == 0)
                    return false;
            }
        }
        return true;
    }

    private void imprimirCampo() {
        System.out.println("\n===== CAMPO MINADO =====");
        System.out.printf("Bandeiras restantes: %02d%n", flagsRemaining);

        for (int i = 0; i < tamanho; i++) {
            for (int j = 0; j < tamanho; j++) {
                if (marcada[i][j]) {
                    System.out.print(" M ");
                } else if (revelado[i][j]) {
                    int val = adjacencias[i][j];
                    if (val == -1) {
                        System.out.print(" * ");
                    } else {
                        if (val == 0) {
                            System.out.print("   ");
                        } else {
                            System.out.print(" " + val + " ");
                        }
                    }
                } else {
                    System.out.print(" ? ");
                }
            }
            System.out.println();
        }
        System.out.println("========================");
    }

    private void realizarJogadaProbabilistica(int jogada) {
        double[][] prob = calcularProbabilidades();
        double best = Double.MAX_VALUE;
        List<int[]> candidatos = new ArrayList<>();
        for (int i = 0; i < tamanho; i++) {
            for (int j = 0; j < tamanho; j++) {
                if (revelado[i][j] || marcada[i][j]) continue;
                if (prob[i][j] < best - 1e-12) {
                    best = prob[i][j];
                    candidatos.clear();
                    candidatos.add(new int[]{i, j});
                } else if (Math.abs(prob[i][j] - best) < 1e-12) {
                    candidatos.add(new int[]{i, j});
                }
            }
        }

        if (candidatos.isEmpty()) {
            realizarJogadaAleatoria(jogada);
            return;
        }

        int[] escolha = candidatos.get(random.nextInt(candidatos.size()));
        int x = escolha[0], y = escolha[1];

        boolean bomba = campo[x][y] == 1;
        int minasAoRedor = adjacencias[x][y];
        revelado[x][y] = true;

        System.out.printf("[JogadorAgent - Prob] Jogada %d: (%d,%d) => %s (P(mina)=%.2f) (%d minas ao redor)%n",
                jogada, x + 1, y + 1,
                bomba ? "BOMBA" : "seguro", best, minasAoRedor);

        if (bomba) {
            imprimirCampo();
            System.out.println("\nBOOM! Fim de jogo!");
            escreverResultado("PERDEU");
            System.exit(0);
        }

        if (minasAoRedor == 0) {
            revelarVizinhosBFS(x, y);
        }

        atualizarArquivoMinas();
        imprimirCampo();
        imprimirTempoAtual();
    }

    private void atualizarArquivoMinas() {
        try {
            int marcadasCount = 0;
            for (int i = 0; i < tamanho; i++) for (int j = 0; j < tamanho; j++) if (marcada[i][j]) marcadasCount++;
            Path p = Paths.get("minas_encontradas.txt");
            Files.writeString(p, String.format("%02d", marcadasCount), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("[JogadorAgent] Erro ao atualizar minas_encontradas.txt: " + e.getMessage());
        }
    }

    private int lerTempoAtual() {
        Path p = Paths.get("tempo.txt");
        if (!Files.exists(p)) return 0;
        try {
            String s = Files.readString(p).trim();
            if (s.isEmpty()) return 0;
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
    }

    private void imprimirTempoAtual() {
        int t = lerTempoAtual();
        System.out.println("[Tempo] Tempo atual: " + t + "s");
    }

    private void escreverResultado(String res) {
        try {
            Files.writeString(Paths.get("resultado.txt"), res);
            Files.writeString(Paths.get("tempo_final.txt"), String.valueOf(lerTempoAtual()));
        } catch (IOException e) {
            System.err.println("[JogadorAgent] Erro ao escrever resultado: " + e.getMessage());
        }
    }
}
