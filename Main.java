import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.*;

/**
 * IntelliGestor — Desafios Ambientais.
 * Interface e ilustrações feitas inteiramente com Swing e Java2D.
 * Compilar: javac -encoding UTF-8 Main.java | Executar: java Main
 * Requer Java 8 ou superior. Nenhum arquivo de imagem ou biblioteca externa.
 */
public final class Main extends JPanel implements MouseListener, MouseMotionListener, ActionListener {
    private static final long serialVersionUID = 1L;
    static final int LARGURA = 1100, ALTURA = 720;
    static final String FONTE = escolherFonte();
    static final Color VERDE = new Color(7, 75, 53), CREME = new Color(255, 249, 222);
    static final Color TINTA = new Color(20, 60, 43), OURO = new Color(255, 194, 47);
    static final String[] TITULOS = {"", "QUEBRA-CABEÇA", "JOGO DA MEMÓRIA", "COLETA SELETIVA"};
    static final Color[] CORES = {new Color(73, 156, 81), new Color(29, 155, 205), new Color(241, 178, 35)};
    final String[] nomesLixo = {"PAPEL", "PLÁSTICO", "VIDRO", "METAL", "ORGÂNICO", "PILHAS", "LÂMPADAS"};
    final String[] residuosOriginais = {"JORNAL", "GARRAFA", "POTE", "LATA", "CASCA", "PILHA", "LAMPADA", "CADERNO", "SACOLA", "VIDRO", "LATINHA", "BANANA", "BATERIA", "LAMPADA"};
    final Random sorteio = new Random();
    final javax.swing.Timer relogio = new javax.swing.Timer(40, this);
    int tela = 0, fase = 1, segundos, movimentos, tentativas, pontos, acertos, erros, progresso;
    int primeiraCarta = -1, segundaCarta = -1, esperaCartas, selecionada = -1;
    int arrastando = -1, cursorX, cursorY, mensagemTempo, relogioContador;
    int hoverX = -1, hoverY = -1;
    String mensagem = "";
    final int[] pecas = new int[9], cartas = new int[12];
    final boolean[] pares = new boolean[12];
    int[] tiposLixo = {0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6};
    String[] objetos = residuosOriginais.clone();
    final Rectangle botaoVoltar = new Rectangle(30, 22, 58, 52);
    final Rectangle botaoCasa = new Rectangle(1014, 22, 58, 52);
    final Rectangle botaoPrincipal = new Rectangle(340, 568, 420, 76);
    transient BufferedImage paisagem, cenario;

    public Main() {
        setPreferredSize(new Dimension(LARGURA, ALTURA));
        setBackground(VERDE);
        addMouseListener(this);
        addMouseMotionListener(this);
        setFocusable(true);
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "voltar");
        getActionMap().put("voltar", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { voltar(); }
        });
        paisagem = new BufferedImage(970, 760, BufferedImage.TYPE_INT_RGB);
        Graphics2D p = paisagem.createGraphics(); qualidade(p);
        EcoArt.landscape(p, 0, 0, 970, 760); p.dispose();
        cenario = new BufferedImage(LARGURA, ALTURA, BufferedImage.TYPE_INT_RGB);
        Graphics2D b = cenario.createGraphics(); qualidade(b);
        EcoArt.background(b, LARGURA, ALTURA); b.dispose();
        iniciarQuebraCabeca(); iniciarMemoria();
        relogio.start();
    }

    void iniciarQuebraCabeca() {
        do {
            for (int i = 0; i < 9; i++) pecas[i] = i;
            for (int i = 8; i > 0; i--) { int j = sorteio.nextInt(i + 1), a = pecas[i]; pecas[i] = pecas[j]; pecas[j] = a; }
        } while (quebraCompleto());
        selecionada = -1; movimentos = 0;
    }
    boolean quebraCompleto() { for (int i = 0; i < 9; i++) if (pecas[i] != i) return false; return true; }
    void iniciarMemoria() {
        for (int i = 0; i < 12; i++) cartas[i] = i % 6;
        for (int i = 11; i > 0; i--) { int j = sorteio.nextInt(i + 1), a = cartas[i]; cartas[i] = cartas[j]; cartas[j] = a; }
        Arrays.fill(pares, false); primeiraCarta = segundaCarta = -1;
        esperaCartas = tentativas = acertos = 0;
    }
    void iniciarColeta() {
        pontos = acertos = erros = 0; arrastando = -1;
        tiposLixo = new int[]{0,1,2,3,4,5,6,0,1,2,3,4,5,6}; objetos = residuosOriginais.clone();
    }
    void iniciarFase(int numero) {
        if (numero < 1 || numero > 3 || numero > progresso + 1) return;
        fase = numero; segundos = relogioContador = mensagemTempo = 0; mensagem = ""; arrastando = -1;
        if (fase == 1) iniciarQuebraCabeca();
        if (fase == 2) iniciarMemoria();
        if (fase == 3) iniciarColeta();
        tela = 2;
    }
    void concluir() { progresso = Math.max(progresso, fase); tela = 3; arrastando = -1; }
    void voltar() { arrastando = -1; selecionada = -1; tela = tela == 1 ? 0 : 1; repaint(); }
    void avisar(String texto) { mensagem = texto; mensagemTempo = 85; }
    public void actionPerformed(ActionEvent evento) {
        if (tela == 2) {
            if (++relogioContador >= 25) { segundos++; relogioContador = 0; }
            if (esperaCartas > 0 && --esperaCartas == 0) primeiraCarta = segundaCarta = -1;
        }
        if (mensagemTempo > 0) mensagemTempo--;
        repaint();
    }

    static void qualidade(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    }
    double escala() { return Math.min(getWidth() / (double)LARGURA, getHeight() / (double)ALTURA); }
    Point local(MouseEvent e) {
        double s = escala(); if (s <= 0) return new Point(-1,-1);
        return new Point((int)((e.getX() - (getWidth()-LARGURA*s)/2)/s), (int)((e.getY()-(getHeight()-ALTURA*s)/2)/s));
    }
    protected void paintComponent(Graphics desenho) {
        super.paintComponent(desenho);
        Graphics2D g = (Graphics2D)desenho.create(); qualidade(g);
        double s = escala(); g.translate((getWidth()-LARGURA*s)/2, (getHeight()-ALTURA*s)/2); g.scale(s,s);
        g.drawImage(cenario, 0, 0, null);
        if (tela == 0) menu(g);
        if (tela == 1) selecao(g);
        if (tela == 2) {
            cabecalho(g, "FASE " + fase + " · " + TITULOS[fase]);
            if (fase == 1) desenharQuebraCabeca(g);
            if (fase == 2) desenharMemoria(g);
            if (fase == 3) desenharColeta(g);
            if (mensagemTempo > 0) {
                painel(g, 235, 661, 630, 38, new Color(255,250,218));
                texto(g, mensagem, 550, 686, 15, VERDE, true);
            }
        }
        if (tela == 3) conclusao(g);
        g.setColor(new Color(13,72,48)); g.setStroke(new BasicStroke(4)); g.drawRoundRect(5,5,1090,710,25,25);
        g.dispose();
    }
    static String escolherFonte() {
        String[] instaladas = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String preferida : new String[]{"DejaVu Sans Condensed", "Arial Narrow", "SansSerif"})
            for (String instalada : instaladas) if (instalada.equalsIgnoreCase(preferida)) return instalada;
        return "SansSerif";
    }
    static Font fonte(int size, boolean titulo) {
        return new Font(titulo ? FONTE : "Dialog", Font.BOLD, size);
    }
    static void texto(Graphics2D g, String t, int x, int y, int size, Color c, boolean centro) {
        g.setFont(fonte(size, false)); g.setColor(c);
        if (centro) x -= g.getFontMetrics().stringWidth(t)/2;
        g.drawString(t,x,y);
    }
    static void titulo(Graphics2D g, String t, int x, int y, int size, Color c) {
        Font f = new Font(FONTE, Font.BOLD, size);
        Graphics2D q=(Graphics2D)g.create(); q.translate(x,y); q.scale(.84,1);
        q.setFont(f); int w=q.getFontMetrics().stringWidth(t);
        q.setColor(new Color(0,24,17,40)); q.drawString(t,-w/2,2);
        q.setColor(c); q.drawString(t,-w/2,0); q.dispose();
    }
    static void painel(Graphics2D g, int x, int y, int w, int h, Color c) {
        g.setColor(new Color(3,59,40,45)); g.fillRoundRect(x+2,y+5,w,h,24,24);
        g.setColor(c); g.fillRoundRect(x,y,w,h,24,24);
        g.setStroke(new BasicStroke(1.4f)); g.setColor(new Color(58,112,62)); g.drawRoundRect(x,y,w,h,24,24);
        g.setColor(new Color(255,255,255,160)); g.drawRoundRect(x+3,y+3,w-6,h-6,20,20);
    }
    void botao(Graphics2D g, Rectangle r, String label, String icon) {
        boolean hover=r.contains(hoverX,hoverY);
        g.setColor(new Color(0,45,29,80)); g.fillRoundRect(r.x,r.y+5,r.width,r.height,50,50);
        g.setPaint(new GradientPaint(0,r.y, new Color(hover?44:33,140,91),0,r.y+r.height,VERDE));
        g.fillRoundRect(r.x,r.y,r.width,r.height,50,50);
        g.setColor(CREME); g.setStroke(new BasicStroke(2.5f)); g.drawRoundRect(r.x,r.y,r.width,r.height,50,50);
        g.setColor(new Color(255,255,255,85)); g.drawRoundRect(r.x+4,r.y+4,r.width-8,r.height-8,44,44);
        titulo(g,label,r.x+r.width/2+(icon.equals("play")?14:0),r.y+r.height/2+10,30,Color.WHITE);
        if (icon.equals("play")) {
            Path2D p=new Path2D.Double(); p.moveTo(r.x+48,r.y+23); p.lineTo(r.x+48,r.y+53); p.lineTo(r.x+72,r.y+38); p.closePath(); g.setColor(CREME); g.fill(p);
        } else if (icon.equals("next")) flecha(g,r.x+r.width-40,r.y+r.height/2,1);
    }
    static void flecha(Graphics2D g,int x,int y,int direcao) {
        g.setColor(Color.WHITE); g.setStroke(new BasicStroke(5,1,1));
        g.drawLine(x-8*direcao,y-9,x+2*direcao,y); g.drawLine(x+2*direcao,y,x-8*direcao,y+9);
    }
    void iconeBotao(Graphics2D g,Rectangle r,boolean casa) {
        painel(g,r.x,r.y,r.width,r.height,VERDE);
        g.setColor(Color.WHITE); g.setStroke(new BasicStroke(4,1,1));
        int x=r.x+r.width/2,y=r.y+r.height/2;
        if (casa) {
            Path2D p=new Path2D.Double(); p.moveTo(x-15,y-2);p.lineTo(x,y-15);p.lineTo(x+15,y-2);p.lineTo(x+11,y-2);p.lineTo(x+11,y+13);p.lineTo(x-11,y+13);p.lineTo(x-11,y-2);p.closePath();g.fill(p);
            g.setColor(VERDE);g.fillRoundRect(x-3,y+3,7,11,2,2);
        } else { g.drawLine(x-12,y,x+14,y); g.drawLine(x-12,y,x-2,y-10); g.drawLine(x-12,y,x-2,y+10); }
    }
    void placa(Graphics2D g,String label,int x,int y,int w) {
        painel(g,x,y,w,53,VERDE); titulo(g,label,x+w/2,y+37,30,CREME);
    }
    void cabecalho(Graphics2D g,String label) {
        iconeBotao(g,botaoVoltar,false); iconeBotao(g,botaoCasa,true);
        placa(g,label,153,22,794);
        if (tela==2) {
            painel(g,760,89,285,36,new Color(255,250,226,235));
            cronometro(g,779,106); texto(g,String.format("Tempo: %02d:%02d",segundos/60,segundos%60),795,112,16,TINTA,false);
            String contagem=fase==1?"Trocas: "+movimentos:fase==2?"Pares: "+acertos+"/6":fase==3?"Pontos: "+pontos:"Achadas: "+acertos+"/5";
            texto(g,contagem,991,112,15,TINTA,true);
        }
    }
    static void cronometro(Graphics2D g,int x,int y) {
        g.setColor(TINTA);g.setStroke(new BasicStroke(2));g.drawOval(x-7,y-7,14,14);g.drawLine(x,y,x,y-4);g.drawLine(x,y,x+3,y+2);g.drawLine(x-3,y-11,x+3,y-11);g.drawLine(x,y-11,x,y-8);
    }

    void menu(Graphics2D g) {
        // Nuvem em tom de papel creme atrás do título e da mascote.
        g.setColor(new Color(255,251,224,240));
        g.fillRoundRect(208,72,684,467,140,140);
        for(int i=0;i<8;i++) g.fillOval(204+i*83,66+(i%2)*8,116,95);
        titulo(g,"INTELLIGESTOR",550,174,76,VERDE);
        titulo(g,"DESAFIOS AMBIENTAIS",550,219,36,VERDE);
        texto(g,"Aprenda brincando a cuidar do planeta!",550,258,22,TINTA,true);
        EcoArt.planet(g,550,438,119);
        botao(g,botaoPrincipal,"INICIAR","play");
        texto(g,"3 desafios · Um planeta melhor",550,678,16,CREME,true);
        Rectangle ajuda=new Rectangle(30,22,58,52); painel(g,ajuda.x,ajuda.y,ajuda.width,ajuda.height,VERDE);
        g.setColor(CREME);g.setStroke(new BasicStroke(4));g.drawOval(46,36,26,26);texto(g,"?",59,57,23,CREME,true);
    }
    Rectangle faseCard(int i) { return new Rectangle(171+i*259,177,239,382); }
    void selecao(Graphics2D g) {
        cabecalho(g,"ESCOLHA A FASE");
        painel(g,155,98,790,49,new Color(255,252,231,240));
        texto(g,"Complete as fases e ajude a cidade a se tornar mais sustentável!",550,130,18,TINTA,true);
        for(int i=0;i<3;i++) {
            Rectangle r=faseCard(i); int x=r.x; boolean livre=i<=progresso;
            painel(g,x,r.y,r.width,r.height,CORES[i]);
            g.setColor(CORES[i].darker()); g.fillOval(x+95,190,49,49);
            titulo(g,""+(i+1),x+119,226,31,Color.WHITE);
            painel(g,x+13,251,213,176,CREME);
            Graphics2D q=(Graphics2D)g.create();q.clip(new RoundRectangle2D.Double(x+17,255,205,168,15,15));
            if(i==0) {
                q.drawImage(paisagem,x+17,255,205,168,null);
                if(i==0) {q.setColor(CREME);q.setStroke(new BasicStroke(2));q.drawLine(x+119,255,x+119,423);q.drawLine(x+17,339,x+222,339);}
                else {q.setColor(VERDE);q.setStroke(new BasicStroke(8));q.drawOval(x+119,323,55,55);q.drawLine(x+167,374,x+191,397);q.setColor(new Color(226,251,255,150));q.fillOval(x+123,327,47,47);}
            } else if(i==1) {
                for(int j=0;j<4;j++) {int cx=x+23+j%2*99,cy=263+j/2*77; painel(q,cx,cy,91,69,j%3==0?new Color(87,184,211):Color.WHITE);if(j%3==0) EcoArt.leaf(q,cx+44,cy+52,33,-.4,VERDE);else ItemArt.memory(q,cx+12,cy+5,67,54,j==1?1:0);}
            } else {for(int j=0;j<3;j++)ItemArt.bin(q,x+24+j*64,314,58,93,j);ItemArt.waste(q,x+83,262,55,51,"GARRAFA");}
            q.dispose();
            String[] a={"QUEBRA-CABEÇA","JOGO DA","COLETA"};String[] b={"","MEMÓRIA","SELETIVA"};
            titulo(g,a[i],x+119,461,23,Color.WHITE);if(!b[i].isEmpty())titulo(g,b[i],x+119,489,23,Color.WHITE);
            if(livre) for(int j=0;j<3;j++) EcoArt.star(g,x+83+j*36,527,14,i<progresso?OURO:new Color(255,255,231,150));
            else {cadeado(g,x+119,521);texto(g,"Conclua a fase anterior",x+119,549,11,Color.WHITE,true);}
        }
        painel(g,86,594,928,83,CREME);EcoArt.star(g,122,634,23,OURO);
        texto(g,"PROGRESSO GERAL",163,621,14,VERDE,false);
        g.setColor(new Color(184,211,153));g.fillRoundRect(163,635,706,15,15,15);
        g.setPaint(new GradientPaint(0,635,new Color(130,196,78),0,650,new Color(65,153,65)));
        g.fillRoundRect(163,635,706*progresso/3,15,15,15);g.setColor(VERDE);g.drawRoundRect(163,635,706,15,15,15);
        texto(g,(progresso*3)+"/9",929,648,18,VERDE,true);
    }
    static void cadeado(Graphics2D g,int x,int y) {g.setStroke(new BasicStroke(3));g.setColor(new Color(99,70,33,180));g.drawRoundRect(x-7,y-12,14,17,12,12);g.fillRoundRect(x-12,y-1,24,20,5,5);g.setColor(CREME);g.fillOval(x-2,y+5,4,5);}
    void instrucao(Graphics2D g,String t) {
        painel(g,55,91,665,37,new Color(255,251,227,240));texto(g,t,387,116,15,TINTA,true);
    }
    Rectangle pecaRect(int i) {return new Rectangle(73+(i%3)*154,175+(i/3)*154,154,154);}
    void desenharQuebraCabeca(Graphics2D g) {
        instrucao(g,"Clique em duas peças para trocar de lugar.");
        painel(g,55,148,498,504,CREME);
        for(int i=0;i<9;i++) {
            Rectangle r=pecaRect(i);int a=pecas[i];
            g.drawImage(paisagem,r.x,r.y,r.x+r.width,r.y+r.height,a%3*970/3,a/3*760/3,(a%3+1)*970/3,(a/3+1)*760/3,null);
        }
        g.setStroke(new BasicStroke(2));g.setColor(new Color(5,85,68));
        for(int j=1;j<3;j++) {
            for(int k=0;k<3;k++) {
                int x=73+j*154,y=175+k*154;Path2D p=new Path2D.Double();p.moveTo(x,y);p.lineTo(x,y+58);p.curveTo(x-31,y+45,x-31,y+108,x,y+95);p.lineTo(x,y+154);g.draw(p);
                x=73+k*154;y=175+j*154;p=new Path2D.Double();p.moveTo(x,y);p.lineTo(x+58,y);p.curveTo(x+45,y-31,x+108,y-31,x+95,y);p.lineTo(x+154,y);g.draw(p);
            }
        }
        if(selecionada>=0){Rectangle r=pecaRect(selecionada);g.setColor(OURO);g.setStroke(new BasicStroke(5));g.drawRect(r.x+3,r.y+3,r.width-6,r.height-6);}
        painel(g,583,148,461,352,CREME);titulo(g,"SUA CIDADE SUSTENTÁVEL",813,184,24,VERDE);
        imagemRedonda(g,paisagem,603,204,421,273,16);
        painel(g,583,520,461,132,CREME);EcoArt.leaf(g,606,550,44,-.5,new Color(76,153,75));
        texto(g,"Cada peça faz a diferença!",843,559,20,VERDE,true);
        texto(g,"Monte a paisagem como no modelo.",813,591,16,TINTA,true);
        texto(g,"9 peças · Troque até completar",813,621,15,TINTA,true);
    }
    static void imagemRedonda(Graphics2D g,BufferedImage img,int x,int y,int w,int h,int raio) {
        Graphics2D q=(Graphics2D)g.create();q.clip(new RoundRectangle2D.Double(x,y,w,h,raio,raio));q.drawImage(img,x,y,w,h,null);q.dispose();g.setColor(VERDE);g.setStroke(new BasicStroke(1.5f));g.drawRoundRect(x,y,w,h,raio,raio);
    }
    Rectangle cartaRect(int i) {return new Rectangle(142+i%6*138,187+i/6*192,122,171);}
    void desenharMemoria(Graphics2D g) {
        instrucao(g,"Encontre os seis pares e descubra atitudes sustentáveis.");
        painel(g,115,154,870,446,CREME);
        for(int i=0;i<12;i++) {
            Rectangle r=cartaRect(i); boolean aberta=pares[i]||i==primeiraCarta||i==segundaCarta;
            painel(g,r.x,r.y,r.width,r.height,aberta?new Color(255,255,239):new Color(137,185,132));
            if(aberta) {
                ItemArt.memory(g,r.x+9,r.y+18,r.width-18,105,cartas[i]);
                String[] labels={"RECICLAR","ENERGIA SOLAR","ENERGIA EÓLICA","PEDALAR","PLANTAR","POUPAR ÁGUA"};
                texto(g,labels[cartas[i]],r.x+r.width/2,r.y+148,11,VERDE,true);
                if(pares[i]) {g.setColor(new Color(55,148,76));g.setStroke(new BasicStroke(3));g.drawRoundRect(r.x+3,r.y+3,r.width-6,r.height-6,20,20);}
            } else {
                g.setColor(new Color(255,255,239,65));g.setStroke(new BasicStroke(1));g.drawRoundRect(r.x+7,r.y+7,r.width-14,r.height-14,16,16);
                EcoArt.leaf(g,r.x+62,r.y+119,59,-.65,new Color(58,125,79,180));
                for(int a=0;a<10;a++){g.setColor(new Color(255,255,255,32));g.drawLine(r.x+13+a*10,r.y+14,r.x+8+a*10,r.y+r.height-14);}
            }
        }
        painel(g,306,619,488,36,CREME);texto(g,"Tentativas: "+tentativas+"   ·   Pares encontrados: "+acertos+"/6",550,643,16,VERDE,true);
    }
    Rectangle residuoRect(int i) {return new Rectangle(57+i%7*142,172+i/7*110,132,99);}
    Rectangle lixeiraRect(int i) {return new Rectangle(45+i*146,413,132,190);}
    void cartaoResiduo(Graphics2D g,int x,int y,int w,int h,String nome,boolean drag) {
        painel(g,x,y,w,h,drag?new Color(255,247,191):new Color(255,253,237));
        ItemArt.waste(g,x+20,y+5,w-40,h-10,nome);
    }
    void desenharColeta(Graphics2D g) {
        instrucao(g,"Arraste cada resíduo para a lixeira da mesma categoria.");
        painel(g,33,149,1034,501,CREME);
        for(int i=0;i<14;i++)if(i>=acertos&&i!=arrastando) {
            Rectangle r=residuoRect(i);cartaoResiduo(g,r.x,r.y,r.width,r.height,objetos[i],false);
        }
        for(int i=0;i<7;i++) {
            Rectangle r=lixeiraRect(i);ItemArt.bin(g,r.x,r.y,r.width,r.height-13,i);
            texto(g,nomesLixo[i],r.x+r.width/2,r.y+151,i>=4?13:15,Color.WHITE,true);
            if(i>=5) texto(g,"COLETA ESPECIAL",r.x+r.width/2,613,10,VERDE,true);
        }
        texto(g,"Acertos: "+acertos+"/14    ·    Erros: "+erros+"    ·    Pilhas e lâmpadas exigem coleta especial.",550,637,15,TINTA,true);
        if(arrastando>=0)cartaoResiduo(g,cursorX-60,cursorY-45,120,90,objetos[arrastando],true);
    }
    void conclusao(Graphics2D g) {
        painel(g,205,88,690,569,new Color(255,251,226,245));
        placa(g,"FASE "+fase+" CONCLUÍDA!",300,42,500);
        EcoArt.planet(g,550,294,108);
        for(int i=0;i<6;i++)EcoArt.star(g,i<3?340+(i%2)*32:760-(i%2)*32,191+(i%3)*73,i%2==0?17:12,OURO);
        titulo(g,fase==2?"MUITO BEM!":fase==3?"EXCELENTE!":"PARABÉNS!",550,454,37,VERDE);
        String[][] frases={{},{"Você completou o quebra-cabeça","e ajudou a construir uma cidade mais sustentável!"},{"Você encontrou todos os pares","e aprendeu mais sobre o meio ambiente!"},{"Você separou corretamente os 14 resíduos","e fez a coleta seletiva!"}};
        texto(g,frases[fase][0],550,490,18,TINTA,true);texto(g,frases[fase][1],550,517,18,TINTA,true);
        texto(g,String.format("Tempo: %02d:%02d",segundos/60,segundos%60),550,549,14,VERDE,true);
        botao(g,botaoPrincipal,fase<3?"CONTINUAR":"VOLTAR AO MENU","next");
    }

    public void mouseClicked(MouseEvent evento) {
        Point p=local(evento);int x=p.x,y=p.y;
        if(tela==0) {
            if(botaoPrincipal.contains(p))tela=1;
            else if(botaoVoltar.contains(p))JOptionPane.showMessageDialog(this,"Complete as três fases para cuidar do planeta!\n\n1. Troque as peças para montar a paisagem.\n2. Encontre os seis pares de cartas.\n3. Arraste os 14 resíduos para suas sete lixeiras.\n\nPressione Esc para voltar. O progresso dura esta sessão.","Como jogar",JOptionPane.INFORMATION_MESSAGE);
        } else if(botaoCasa.contains(p)&&(tela==1||tela==2)) {tela=0;arrastando=-1;}
        else if(tela==1) {
            if(botaoVoltar.contains(p))tela=0;
            else for(int i=0;i<3;i++)if(faseCard(i).contains(p))iniciarFase(i+1);
        } else if(tela==3) {
            if(botaoPrincipal.contains(p)) {if(fase==3)tela=0;else iniciarFase(fase+1);}
        } else if(tela==2) {
            if(botaoVoltar.contains(p)){voltar();return;}
            if(fase==1) {
                for(int i=0;i<9;i++)if(pecaRect(i).contains(p)) {
                    if(selecionada<0)selecionada=i;
                    else if(selecionada==i)selecionada=-1;
                    else {int a=pecas[selecionada];pecas[selecionada]=pecas[i];pecas[i]=a;selecionada=-1;movimentos++;if(quebraCompleto())concluir();}
                    break;
                }
            } else if(fase==2) {
                for(int i=0;i<12;i++)if(cartaRect(i).contains(p)) {
                    if(esperaCartas>0||pares[i]||i==primeiraCarta)return;
                    if(primeiraCarta<0)primeiraCarta=i;
                    else {segundaCarta=i;tentativas++;
                        if(cartas[primeiraCarta]==cartas[segundaCarta]){pares[primeiraCarta]=pares[segundaCarta]=true;acertos++;primeiraCarta=segundaCarta=-1;if(acertos==6)concluir();}
                        else esperaCartas=25;
                    }break;
                }
            }
        }
        repaint();
    }
    public void mousePressed(MouseEvent evento) {
        if(tela!=2||fase!=3)return;Point p=local(evento);
        for(int i=acertos;i<14;i++)if(residuoRect(i).contains(p)){arrastando=i;cursorX=p.x;cursorY=p.y;break;}
        repaint();
    }
    public void mouseDragged(MouseEvent evento) {Point p=local(evento);cursorX=p.x;cursorY=p.y;repaint();}
    public void mouseReleased(MouseEvent evento) {
        if(arrastando<0)return;Point p=local(evento);
        if(tela==2&&fase==3)for(int i=0;i<7;i++)if(lixeiraRect(i).contains(p)) {
            if(tiposLixo[arrastando]==i) {
                int a=tiposLixo[arrastando];tiposLixo[arrastando]=tiposLixo[acertos];tiposLixo[acertos]=a;
                String n=objetos[arrastando];objetos[arrastando]=objetos[acertos];objetos[acertos]=n;
                acertos++;pontos+=10;avisar("Correto! +10 pontos");if(acertos==14)concluir();
            } else {erros++;pontos=Math.max(0,pontos-2);avisar("Lixeira incorreta. Tente outra!");}break;
        }
        arrastando=-1;repaint();
    }
    public void mouseMoved(MouseEvent evento) {
        Point p=local(evento);hoverX=p.x;hoverY=p.y;
        boolean clicavel=botaoPrincipal.contains(p)&&(tela==0||tela==3)||botaoVoltar.contains(p)&&tela!=3||botaoCasa.contains(p)&&(tela==1||tela==2);
        if(tela==1)for(int i=0;i<3;i++)clicavel|=i<=progresso&&faseCard(i).contains(p);
        if(tela==2) {
            if(fase==1)for(int i=0;i<9;i++)clicavel|=pecaRect(i).contains(p);
            if(fase==2)for(int i=0;i<12;i++)clicavel|=!pares[i]&&cartaRect(i).contains(p);
            if(fase==3)for(int i=acertos;i<14;i++)clicavel|=residuoRect(i).contains(p);
        }
        setCursor(Cursor.getPredefinedCursor(clicavel?Cursor.HAND_CURSOR:Cursor.DEFAULT_CURSOR));repaint();
    }
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {hoverX=hoverY=-1;repaint();}
    public static void main(String[] args) {
        SwingUtilities.invokeLater(()->{
            JFrame janela=new JFrame("IntelliGestor · Desafios Ambientais");
            janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);janela.setContentPane(new Main());
            janela.pack();janela.setMinimumSize(new Dimension(790,545));janela.setLocationRelativeTo(null);janela.setVisible(true);
        });
    }
}


/** Ilustracoes vetoriais originais: nenhuma imagem ou fonte de icones externa. */
class EcoArt {
    private static final Color PINE = new Color(12, 76, 55);
    private static final Color LEAF = new Color(62, 142, 65);
    private static final Color LIME = new Color(145, 194, 64);
    private static Graphics2D copy(Graphics2D source) {
        Graphics2D g = (Graphics2D) source.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        return g;
    }
    private static Path2D path(double... xy) {
        Path2D p = new Path2D.Double();
        p.moveTo(xy[0], xy[1]);
        for (int i = 2; i < xy.length; i += 2) p.lineTo(xy[i], xy[i + 1]);
        p.closePath();
        return p;
    }
    private static void fill(Graphics2D g, Shape shape, Color fill, Color outline) {
        g.setColor(fill); g.fill(shape);
        if (outline != null) { g.setColor(outline); g.draw(shape); }
    }
    public static void background(Graphics2D source, int w, int h) {
        Graphics2D g = copy(source);
        g.scale(w / 1100.0, h / 720.0);
        g.setPaint(new GradientPaint(0, 0, new Color(137, 216, 220), 0, 720, new Color(244, 247, 208)));
        g.fillRect(0, 0, 1100, 720);
        cloud(g, 70, 78, 1.5); cloud(g, 925, 46, 1.65); cloud(g, 530, 118, 1.7);
        g.setColor(new Color(255, 252, 224, 155));
        g.fillOval(165, -15, 785, 790);
        for (int i = 0; i < 12; i++) {
            int x = 5 + i * 94, height = 88 + i * 29 % 125;
            building(g, x, 493 - height, 48 + i % 3 * 7, height, i);
        }
        turbine(g, 172, 478, 118); turbine(g, 950, 442, 159);
        g.setColor(new Color(135, 194, 100)); g.fillOval(-160, 430, 850, 340);
        g.setColor(new Color(158, 205, 90)); g.fillOval(585, 436, 720, 300);
        g.setColor(new Color(78, 159, 82)); g.fillOval(-210, 541, 750, 280);
        g.setColor(new Color(96, 180, 81)); g.fillOval(620, 539, 760, 270);
        Path2D river = new Path2D.Double(); river.moveTo(730, 500);
        river.curveTo(550, 556, 917, 580, 744, 632); river.curveTo(610, 674, 380, 624, 260, 720);
        river.lineTo(777, 720); river.curveTo(996, 635, 754, 597, 866, 549); river.closePath();
        g.setPaint(new GradientPaint(0, 510, new Color(80, 192, 209), 0, 720, new Color(36, 155, 180)));
        g.fill(river);
        g.setColor(new Color(218, 249, 214)); g.setStroke(new BasicStroke(3));
        g.draw(new CubicCurve2D.Double(730, 500, 530, 563, 920, 580, 744, 632));
        for (int i = 0; i < 7; i++) ripple(g, 690 + i * 13 % 130, 560 + i * 19, 29);
        tree(g, 25, 587, 1.40, 0); tree(g, 1071, 597, 1.58, 2);
        tree(g, 107, 610, .75, 2); tree(g, 1006, 605, .85, 1);
        shrub(g, 0, 650, 135, 55); shrub(g, 960, 666, 165, 65);
        // Foliage forms a frame without competing with the middle of the screen.
        Random r = new Random(527);
        for (int side = 0; side < 2; side++) {
            for (int i = 0; i < 12; i++) {
                double x = side == 0 ? r.nextInt(86) - 25 : 1100 - r.nextInt(86) + 25;
                double y = 30 + i * 61;
                leaf(g, x, y, 55 + r.nextInt(30), side == 0 ? .7 : -.9, i % 2 == 0 ? LEAF : new Color(79, 157, 71));
            }
        }
        for (int i = 0; i < 20; i++) {
            double x = i * 60 - 15, y = 718 + i % 3 * 7;
            leaf(g, x, y, 42 + i % 4 * 9, -.10 + (i % 5 - 2) * .22, i % 2 == 0 ? PINE : LEAF);
        }
        for (int i = 0; i < 7; i++) {
            leaf(g, i * 29 + 8, 0, 48 - i * 2, 1.35 + i * .15, i % 2 == 0 ? LEAF : LIME);
            leaf(g, 1090 - i * 29, 0, 48 - i * 2, -2.55 + i * .10, i % 2 == 0 ? LEAF : LIME);
        }
        for (int i = 0; i < 12; i++) flower(g, 30 + i * 94, 695 + i % 2 * 13, 5, new Color(255, 217, 89));
        g.dispose();
    }
    public static void landscape(Graphics2D source, int x, int y, int w, int h) {
        Graphics2D g = copy(source); g.translate(x, y); g.scale(w / 720.0, h / 470.0);
        g.clipRect(0, 0, 720, 470);
        g.setPaint(new GradientPaint(0, 0, new Color(78, 188, 233), 0, 345, new Color(221, 247, 202)));
        g.fillRect(0, 0, 720, 470);
        g.setColor(new Color(255, 235, 123, 200)); g.fillOval(536, 28, 61, 61);
        cloud(g, 90, 44, .85); cloud(g, 316, 29, 1.05); cloud(g, 626, 68, 1.0);
        for (int i = 0; i < 13; i++) {
            int bx = i * 59 - 4, bh = 83 + i * 43 % 118;
            building(g, bx, 236 - bh, 40 + i % 2 * 9, bh, i);
        }
        turbine(g, 472, 220, 147); turbine(g, 588, 221, 105);
        g.setColor(new Color(142, 193, 87)); g.fillOval(-85, 203, 480, 150);
        g.fillOval(381, 205, 431, 148);
        g.setColor(new Color(82, 163, 69)); g.fillOval(-55, 241, 341, 145);
        g.setColor(new Color(113, 181, 72)); g.fillOval(461, 238, 335, 145);
        Path2D water = new Path2D.Double(); water.moveTo(350, 251);
        water.curveTo(340, 310, 510, 302, 454, 350); water.curveTo(407, 393, 197, 344, 122, 470);
        water.lineTo(584, 470); water.curveTo(673, 402, 480, 384, 531, 328);
        water.curveTo(560, 292, 424, 291, 440, 251); water.closePath();
        g.setPaint(new GradientPaint(0, 245, new Color(62, 183, 219), 0, 470, new Color(28, 144, 196)));
        g.fill(water);
        g.setColor(new Color(218, 238, 148)); g.setStroke(new BasicStroke(6));
        g.draw(new CubicCurve2D.Double(350, 251, 340, 310, 510, 302, 454, 350));
        g.draw(new CubicCurve2D.Double(454, 350, 407, 393, 197, 344, 122, 470));
        g.setColor(new Color(225, 209, 137));
        Path2D walk = new Path2D.Double(); walk.moveTo(0, 358);
        walk.curveTo(171, 307, 189, 351, 299, 321); walk.lineTo(313, 342);
        walk.curveTo(176, 392, 131, 350, 0, 409); walk.closePath(); g.fill(walk);
        bridge(g, 235, 272, 295);
        tree(g, 61, 330, 1.01, 2); tree(g, 133, 294, .72, 1);
        tree(g, 652, 333, 1.16, 1); tree(g, 568, 304, .73, 0);
        tree(g, 21, 448, .87, 1); tree(g, 691, 448, .91, 2);
        tree(g, 168, 400, .55, 0); tree(g, 603, 423, .69, 2);
        shrub(g, 2, 447, 160, 37); shrub(g, 540, 448, 208, 40);
        bench(g, 67, 376, .70); lamp(g, 31, 379, .78);
        bench(g, 525, 371, .62); lamp(g, 625, 370, .63);
        for (int i = 0; i < 16; i++) {
            int rx = 290 + i * 67 % 235, ry = 315 + i * 31 % 146;
            ripple(g, rx, ry, 12 + i % 4 * 6);
        }
        for (int i = 0; i < 24; i++) {
            double fx = i * 33 % 720, fy = 406 + i * 13 % 59;
            if (fx < 175 || fx > 562) flower(g, fx, fy, 3 + i % 2, i % 3 == 0 ? new Color(255, 228, 93) : new Color(255, 245, 188));
        }
        g.setColor(new Color(238, 231, 158));
        g.fillOval(475, 394, 17, 7); g.fillOval(206, 406, 15, 6);
        g.dispose();
    }
    private static void building(Graphics2D g, int x, int y, int w, int h, int i) {
        Color[] colors = {new Color(83, 164, 183), new Color(91, 183, 202), new Color(247, 184, 99), new Color(128, 190, 184)};
        fill(g, new RoundRectangle2D.Double(x, y, w, h, 2, 2), colors[i % 4], new Color(84, 146, 149, 130));
        g.setColor(new Color(37, 115, 149, 80)); g.fillRect(x + w - 8, y + 1, 8, h - 1);
        g.setColor(new Color(243, 241, 178));
        for (int a = 8; a < h - 8; a += 18) for (int b = 7; b < w - 8; b += 13) g.fillRoundRect(x + b, y + a, 5, 9, 1, 1);
        if (i % 3 == 1) { g.setColor(new Color(74, 139, 155)); g.fillRect(x + w / 2, y - 12, 3, 12); }
    }
    private static void cloud(Graphics2D g, double x, double y, double s) {
        Graphics2D q = copy(g); q.translate(x, y); q.scale(s, s);
        q.setColor(new Color(255, 254, 230, 230));
        q.fill(new Ellipse2D.Double(-48, 0, 98, 21));
        q.fill(new Ellipse2D.Double(-31, -17, 37, 37));
        q.fill(new Ellipse2D.Double(-7, -28, 47, 47)); q.dispose();
    }
    private static void turbine(Graphics2D g, double x, double y, double h) {
        Graphics2D q = copy(g); q.translate(x, y); q.scale(h / 130, h / 130);
        fill(q, path(-4, 0, 4, 0, 2, -97, -2, -97), new Color(237, 240, 209), new Color(106, 164, 164));
        q.translate(0, -100);
        for (int i = 0; i < 3; i++) {
            q.rotate(Math.PI * 2 / 3);
            Path2D blade = new Path2D.Double(); blade.moveTo(0, 1); blade.curveTo(-8, -13, -8, -42, 0, -62); blade.curveTo(6, -37, 4, -16, 0, 1);
            fill(q, blade, new Color(242, 249, 224), new Color(120, 170, 169));
        }
        fill(q, new Ellipse2D.Double(-5, -5, 10, 10), new Color(209, 231, 211), new Color(91, 155, 153)); q.dispose();
    }
    private static void bridge(Graphics2D g, double x, double y, double w) {
        Graphics2D q = copy(g); q.translate(x, y); q.scale(w / 295, 1);
        q.setColor(new Color(40, 100, 83)); q.fillRect(9, 9, 280, 15);
        q.setColor(new Color(159, 190, 151)); q.fillRect(18, 20, 15, 27); q.fillRect(265, 20, 15, 27);
        q.setColor(new Color(230, 218, 161)); q.fillRoundRect(0, 1, 295, 12, 5, 5);
        q.setColor(new Color(22, 98, 76)); q.setStroke(new BasicStroke(3));
        q.drawLine(0, -10, 295, -10); q.drawLine(0, -29, 295, -29);
        for (int i = 1; i < 14; i++) q.drawLine(i * 21, -29, i * 21, 0);
        q.dispose();
    }
    private static void tree(Graphics2D g, double x, double y, double s, int variant) {
        Graphics2D q = copy(g); q.translate(x, y); q.scale(s, s);
        Color canopy = variant == 0 ? new Color(52, 149, 71) : variant == 1 ? new Color(98, 171, 58) : new Color(133, 184, 61);
        q.setColor(new Color(52, 118, 54, 90)); q.fillOval(-43, -6, 87, 13);
        fill(q, path(-6, 0, -8, -64, 7, -64, 5, 0), new Color(119, 80, 44), new Color(88, 82, 43));
        q.setColor(new Color(101, 128, 53)); q.fillOval(-46, -110, 83, 72);
        fill(q, new Ellipse2D.Double(-49, -126, 77, 75), canopy, new Color(58, 130, 58));
        fill(q, new Ellipse2D.Double(-8, -119, 57, 69), canopy, new Color(58, 130, 58));
        fill(q, new Ellipse2D.Double(-24, -151, 59, 78), canopy.brighter(), new Color(71, 143, 60));
        q.setColor(new Color(182, 209, 81, 120)); q.fillOval(-29, -122, 18, 27); q.fillOval(11, -126, 12, 19);
        q.setColor(new Color(104, 76, 42)); q.setStroke(new BasicStroke(4));
        q.drawLine(0, -20, -1, -91); q.drawLine(-1, -61, -19, -83); q.drawLine(-1, -43, 20, -66); q.dispose();
    }
    private static void shrub(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(new Color(69, 143, 63)); g.fillOval(x, y - h / 2, w, h);
        for (int i = 0; i < 7; i++) {
            g.setColor(i % 2 == 0 ? new Color(118, 179, 65) : new Color(87, 161, 60));
            g.fillOval(x + i * w / 8, y - h / 2 - i % 3 * 5, w / 4, h);
        }
    }
    private static void bench(Graphics2D g, double x, double y, double s) {
        Graphics2D q = copy(g); q.translate(x, y); q.scale(s, s);
        q.setColor(new Color(34, 87, 61)); q.fillRoundRect(7, -32, 5, 57, 3, 3); q.fillRoundRect(72, -32, 5, 57, 3, 3);
        for (int i = 0; i < 3; i++) fill(q, new RoundRectangle2D.Double(0, -38 + i * 10, 85, 7, 3, 3), new Color(183, 119, 54), new Color(111, 80, 43));
        fill(q, new RoundRectangle2D.Double(-3, -4, 91, 10, 3, 3), new Color(197, 137, 63), new Color(103, 78, 42)); q.dispose();
    }
    private static void lamp(Graphics2D g, double x, double y, double s) {
        Graphics2D q = copy(g); q.translate(x, y); q.scale(s, s);
        q.setColor(new Color(39, 91, 66)); q.fillRoundRect(-3, -105, 6, 105, 3, 3); q.fillRoundRect(-8, -3, 16, 5, 2, 2);
        fill(q, path(-10, -107, -14, -137, 14, -137, 10, -107), new Color(253, 248, 191), new Color(33, 86, 66));
        fill(q, path(-18, -138, 0, -153, 18, -138), new Color(44, 103, 75), new Color(33, 86, 66));
        q.setColor(new Color(48, 98, 72)); q.drawLine(0, -137, 0, -107); q.dispose();
    }
    private static void ripple(Graphics2D g, int x, int y, int w) {
        g.setColor(new Color(207, 249, 228, 170)); g.setStroke(new BasicStroke(1.7f));
        g.draw(new QuadCurve2D.Double(x, y, x + w / 2.0, y - 4, x + w, y));
    }
    public static void leaf(Graphics2D source, double x, double y, double size, double angle, Color color) {
        Graphics2D g = copy(source); g.translate(x, y); g.rotate(angle); g.scale(size / 60, size / 60);
        Path2D p = new Path2D.Double(); p.moveTo(0, 0); p.curveTo(-16, -32, 1, -57, 28, -62); p.curveTo(36, -32, 25, -10, 0, 0);
        fill(g, p, color, color.darker());
        g.setColor(color.darker()); g.setStroke(new BasicStroke(1.15f)); g.drawLine(0, 0, 23, -53);
        g.drawLine(8, -19, 1, -34); g.drawLine(13, -31, 27, -35); g.drawLine(18, -42, 13, -49); g.dispose();
    }
    public static void star(Graphics2D source, double cx, double cy, double r, Color color) {
        Graphics2D g = copy(source); Path2D p = new Path2D.Double();
        for (int i = 0; i < 10; i++) {
            double a = -Math.PI / 2 + i * Math.PI / 5, rr = i % 2 == 0 ? r : r * .47;
            double xx = cx + Math.cos(a) * rr, yy = cy + Math.sin(a) * rr;
            if (i == 0) p.moveTo(xx, yy); else p.lineTo(xx, yy);
        }
        p.closePath(); fill(g, p, color, color.darker()); g.dispose();
    }
    private static void flower(Graphics2D source, double x, double y, double r, Color color) {
        Graphics2D g = copy(source); g.translate(x, y);
        for (int i = 0; i < 5; i++) {
            double a = i * Math.PI * 2 / 5;
            g.setColor(color); g.fill(new Ellipse2D.Double(Math.cos(a) * r - r * .65, Math.sin(a) * r - r * .65, r * 1.3, r * 1.3));
        }
        fill(g, new Ellipse2D.Double(-r * .45, -r * .45, r * .9, r * .9), new Color(255, 185, 45), null); g.dispose();
    }
    public static void planet(Graphics2D source, int cx, int cy, int radius) {
        Graphics2D g = copy(source); g.translate(cx, cy); g.scale(radius / 100.0, radius / 100.0);
        // Waving arms and five little fingers are built from strokes and paths.
        for (int side = -1; side <= 1; side += 2) {
            Graphics2D arm = copy(g); arm.scale(side, 1);
            arm.setStroke(new BasicStroke(13, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            arm.setColor(new Color(19, 151, 201)); arm.draw(new CubicCurve2D.Double(79, 10, 109, 7, 120, -15, 124, -37));
            arm.setColor(new Color(52, 194, 229)); arm.setStroke(new BasicStroke(9, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            arm.draw(new CubicCurve2D.Double(80, 8, 109, 5, 118, -16, 123, -38));
            arm.fillOval(117, -48, 17, 21);
            arm.setStroke(new BasicStroke(5.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            arm.drawLine(121, -40, 110, -49); arm.drawLine(123, -46, 119, -60);
            arm.drawLine(127, -46, 127, -63); arm.drawLine(131, -43, 137, -57); arm.drawLine(132, -37, 144, -45); arm.dispose();
        }
        g.setColor(new Color(16, 104, 86, 35)); g.fillOval(-84, 81, 176, 24);
        g.setPaint(new RadialGradientPaint(new Point2D.Double(-35, -40), 160, new float[]{0, .65f, 1}, new Color[]{new Color(77, 206, 229), new Color(18, 163, 201), new Color(5, 118, 171)}));
        Ellipse2D sphere = new Ellipse2D.Double(-100, -100, 200, 200); g.fill(sphere);
        Graphics2D continents = copy(g); continents.clip(sphere);
        continents.setColor(new Color(54, 165, 70));
        Path2D a = new Path2D.Double(); a.moveTo(-105, -45); a.curveTo(-75, -82, -38, -102, -8, -83);
        a.curveTo(-31, -62, -14, -45, -33, -31); a.curveTo(-46, -21, -42, -5, -73, 3); a.curveTo(-93, 3, -83, 35, -110, 28); a.closePath(); continents.fill(a);
        Path2D b = new Path2D.Double(); b.moveTo(46, -95); b.curveTo(17, -62, 32, -46, 61, -41);
        b.curveTo(81, -51, 97, -19, 105, -26); b.lineTo(113, -94); b.closePath(); continents.fill(b);
        Path2D c = new Path2D.Double(); c.moveTo(-45, 34); c.curveTo(-24, 17, -10, 34, 8, 45);
        c.curveTo(23, 65, -6, 82, -6, 111); c.lineTo(-54, 109); c.curveTo(-71, 78, -66, 59, -45, 34); c.closePath(); continents.fill(c);
        continents.setColor(new Color(88, 184, 66)); continents.fillOval(67, 12, 47, 68); continents.fillOval(-81, -71, 43, 34); continents.dispose();
        g.setStroke(new BasicStroke(2.2f)); g.setColor(new Color(7, 121, 137)); g.draw(sphere);
        for (int side = -1; side <= 1; side += 2) {
            double ex = side * 32;
            fill(g, new Ellipse2D.Double(ex - 22, -34, 43, 53), Color.WHITE, new Color(33, 130, 130));
            fill(g, new Ellipse2D.Double(ex - 11, -25, 25, 34), new Color(7, 41, 45), null);
            g.setColor(Color.WHITE); g.fill(new Ellipse2D.Double(ex - 7, -22, 8, 11));
            g.setColor(new Color(241, 144, 127, 145)); g.fill(new Ellipse2D.Double(ex - 16, 20, 25, 10));
            g.setColor(PINE); g.setStroke(new BasicStroke(2.6f)); g.draw(new QuadCurve2D.Double(ex - 17, -42, ex - 5, -49, ex + 10, -42));
        }
        Path2D mouth = new Path2D.Double(); mouth.moveTo(-12, 24); mouth.quadTo(0, 33, 12, 24); mouth.curveTo(10, 54, -11, 54, -12, 24); mouth.closePath();
        fill(g, mouth, new Color(9, 59, 59), null); g.setColor(new Color(244, 123, 111)); g.fillOval(-7, 39, 14, 8);
        for (int i = 0; i < 9; i++) {
            double xx = -84 + i * 21, yy = -84 - Math.sin(i * Math.PI / 8) * 20;
            leaf(g, xx - 2, yy + 5, 34, -.9 + i * .2, i % 2 == 0 ? PINE : LEAF);
        }
        Color[] flowers = {new Color(49, 179, 226), new Color(255, 246, 181), new Color(255, 187, 58), new Color(255, 125, 58), new Color(243, 92, 81)};
        for (int i = 0; i < 7; i++) {
            double xx = -76 + i * 25, yy = -78 - Math.sin(i * Math.PI / 6) * 25;
            flower(g, xx, yy, i == 3 ? 15 : 11, flowers[i % flowers.length]);
        }
        g.dispose();
    }
}


/** Ilustrações vetoriais; não depende de imagens, fontes de ícones ou bibliotecas externas. */
class ItemArt {
    private static final Color INK = new Color(24, 76, 61);
    private static final Color[] COLORS = {
        new Color(12, 142, 204), new Color(238, 68, 56), new Color(26, 158, 72),
        new Color(245, 181, 20), new Color(135, 86, 37), new Color(140, 96, 175), new Color(57, 157, 163)
    };

    private static Graphics2D begin(Graphics2D source, int x, int y, int w, int h) {
        Graphics2D g = (Graphics2D) source.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        double scale = Math.min(w, h) / 100.0;
        g.translate(x + (w - 100 * scale) / 2, y + (h - 100 * scale) / 2);
        g.scale(scale, scale);
        g.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        return g;
    }
    private static void shape(Graphics2D g, Shape s, Color color) {
        g.setColor(color); g.fill(s); g.setColor(INK); g.draw(s);
    }
    private static void gradient(Graphics2D g, Shape s, Color light, Color dark) {
        Rectangle2D b = s.getBounds2D();
        g.setPaint(new GradientPaint((float)b.getMinX(), (float)b.getMinY(), light,
                                   (float)b.getMaxX(), (float)b.getMaxY(), dark));
        g.fill(s); g.setColor(INK); g.draw(s);
    }
    private static Path2D path(double... points) {
        Path2D p = new Path2D.Double(); p.moveTo(points[0], points[1]);
        for (int i = 2; i < points.length; i += 2) p.lineTo(points[i], points[i + 1]);
        p.closePath(); return p;
    }
    private static void line(Graphics2D g, double x, double y, double xx, double yy, Color c) {
        g.setColor(c); g.draw(new Line2D.Double(x, y, xx, yy));
    }
    private static void shadow(Graphics2D g) {
        g.setColor(new Color(30, 80, 50, 22)); g.fill(new Ellipse2D.Double(17, 89, 68, 8));
    }

    static void recycle(Graphics2D source, int cx, int cy, int size, Color color) {
        Graphics2D g = (Graphics2D)source.create();
        g.translate(cx, cy); g.scale(size / 100.0, size / 100.0);
        g.setColor(color);
        for (int i = 0; i < 3; i++) {
            g.fill(path(-31,-8, -9,-45, 6,-20, -3,-20, -14,-2, -23,-7, -27,0));
            g.rotate(Math.PI * 2 / 3);
        }
        g.dispose();
    }

    static void bin(Graphics2D source, int x, int y, int w, int h, int type) {
        Graphics2D g = (Graphics2D)source.create();
        g.translate(x, y); g.scale(w / 100.0, h / 100.0);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(1.4f));
        Color c = COLORS[Math.floorMod(type, COLORS.length)];
        g.setColor(new Color(22, 55, 39, 35)); g.fill(new Ellipse2D.Double(8, 91, 88, 9));
        Path2D body = new Path2D.Double(); body.moveTo(9,20); body.lineTo(17,89);
        body.quadTo(17,97,50,97); body.quadTo(83,97,83,89); body.lineTo(91,20); body.closePath();
        gradient(g,body,c.brighter(),c.darker());
        g.setColor(new Color(255,255,255,58)); g.fill(path(17,29,24,31,29,87,22,85));
        g.setColor(new Color(0,50,25,28)); g.fill(path(78,29,85,27,78,87,72,91));
        gradient(g,new RoundRectangle2D.Double(3,12,94,15,9,9),c.brighter(),c.darker());
        gradient(g,new Ellipse2D.Double(5,8,90,13),c,c.darker());
        g.setColor(new Color(255,255,255,120)); g.draw(new Arc2D.Double(11,11,77,7,5,165,Arc2D.OPEN));
        recycle(g,50,57,44,Color.WHITE);
        g.dispose();
    }

    static void waste(Graphics2D source, int x, int y, int w, int h, String name) {
        Graphics2D g = begin(source,x,y,w,h);
        shadow(g);
        String item = name.toUpperCase(java.util.Locale.ROOT);
        switch (item) {
            case "JORNAL": paper(g,false); break;
            case "CADERNO": paper(g,true); break;
            case "GARRAFA": bottle(g); break;
            case "SACOLA": bag(g); break;
            case "VIDRO": case "POTE": jar(g); break;
            case "LATA": case "LATINHA": can(g); break;
            case "CASCA": apple(g); break;
            case "BANANA": banana(g); break;
            case "PILHA": case "BATERIA": battery(g,item.equals("BATERIA")); break;
            case "LAMPADA": case "LÂMPADA": bulb(g); break;
            default: paper(g,false);
        }
        g.dispose();
    }
    private static void paper(Graphics2D g, boolean notebook) {
        g.rotate(-.13,50,50);
        gradient(g,new RoundRectangle2D.Double(20,13,59,77,4,4),new Color(255,255,245),new Color(211,222,204));
        if (notebook) {
            shape(g,new RoundRectangle2D.Double(21,13,10,77,3,3),new Color(80,159,102));
            g.setColor(new Color(228,174,75));
            for (int yy=20; yy<89; yy+=11) g.draw(new Arc2D.Double(16,yy,11,7,50,270,Arc2D.OPEN));
            shape(g,new RoundRectangle2D.Double(39,30,30,20,3,3),new Color(255,250,221));
            line(g,44,37,64,37,new Color(74,130,106)); line(g,44,43,59,43,new Color(74,130,106));
        } else {
            g.setColor(new Color(50,94,78)); g.fill(new Rectangle2D.Double(29,23,39,7));
            shape(g,new Rectangle2D.Double(29,38,17,23),new Color(134,182,156));
            for (int yy=38; yy<65; yy+=7) line(g,53,yy,71,yy,new Color(103,137,115));
            for (int yy=70; yy<84; yy+=6) line(g,29,yy,70,yy,new Color(103,137,115));
            g.setColor(new Color(222,230,216)); g.fill(path(65,13,79,13,79,28));
        }
    }
    private static void bottle(Graphics2D g) {
        g.rotate(.28,50,50);
        Path2D body = new Path2D.Double(); body.moveTo(40,18); body.lineTo(61,18); body.lineTo(61,29);
        body.curveTo(62,37,73,39,73,49); body.lineTo(73,84); body.quadTo(73,92,63,92);
        body.lineTo(38,92); body.quadTo(28,92,28,84); body.lineTo(28,49);
        body.curveTo(28,39,40,37,40,29); body.closePath();
        gradient(g,body,new Color(149,235,254),new Color(49,172,219));
        shape(g,new RoundRectangle2D.Double(38,9,25,13,4,4),new Color(21,136,203));
        shape(g,new RoundRectangle2D.Double(30,52,41,20,3,3),new Color(169,233,243));
        line(g,35,79,66,79,new Color(26,147,193)); line(g,36,85,65,85,new Color(26,147,193));
        g.setStroke(new BasicStroke(4)); line(g,38,40,35,48,new Color(230,255,255,170));
        line(g,35,54,35,67,new Color(230,255,255,150));
    }
    private static void bag(Graphics2D g) {
        Path2D bag = new Path2D.Double(); bag.moveTo(24,24); bag.lineTo(35,17); bag.lineTo(39,36);
        bag.lineTo(61,36); bag.lineTo(65,17); bag.lineTo(76,24); bag.lineTo(82,86);
        bag.quadTo(51,98,18,86); bag.closePath();
        gradient(g,bag,new Color(250,152,145),new Color(229,77,75));
        g.setColor(new Color(255,255,255,65)); g.fill(path(25,41,32,44,31,83,24,81));
        recycle(g,51,66,34,new Color(255,243,220));
    }
    private static void jar(Graphics2D g) {
        g.rotate(-.12,50,50);
        gradient(g,new RoundRectangle2D.Double(25,27,51,64,12,12),new Color(184,238,237,190),new Color(107,198,207,210));
        shape(g,new RoundRectangle2D.Double(26,18,49,15,5,5),new Color(132,207,209));
        shape(g,new Ellipse2D.Double(26,16,49,10),new Color(190,240,234));
        g.setColor(new Color(255,255,255,130)); g.setStroke(new BasicStroke(4));
        g.draw(new Line2D.Double(34,41,34,77));
        g.setColor(new Color(53,150,159,80)); g.draw(new Arc2D.Double(32,70,36,15,180,180,Arc2D.OPEN));
    }
    private static void can(Graphics2D g) {
        g.rotate(.19,50,50);
        gradient(g,new RoundRectangle2D.Double(28,17,45,73,9,9),new Color(255,112,90),new Color(218,50,41));
        shape(g,new Ellipse2D.Double(28,13,45,15),new Color(215,224,214));
        shape(g,new Ellipse2D.Double(39,18,21,6),new Color(92,112,104));
        g.setColor(new Color(249,244,220)); g.setStroke(new BasicStroke(4));
        g.draw(new Line2D.Double(42,35,39,71)); g.draw(new Arc2D.Double(37,57,21,19,190,170,Arc2D.OPEN));
        g.setColor(new Color(255,255,255,85)); g.draw(new Line2D.Double(33,33,33,76));
        line(g,35,85,64,85,new Color(179,48,39));
    }
    private static void apple(Graphics2D g) {
        shape(g,new RoundRectangle2D.Double(43,14,7,18,3,3),new Color(136,92,45));
        Path2D skin = new Path2D.Double(); skin.moveTo(47,30); skin.curveTo(26,12,15,36,28,45);
        skin.curveTo(42,49,33,67,23,75); skin.curveTo(23,91,43,94,49,88);
        skin.curveTo(63,97,80,83,74,73); skin.curveTo(60,68,61,48,73,43);
        skin.curveTo(85,23,62,16,47,30); skin.closePath();
        gradient(g,skin,new Color(255,240,176),new Color(249,211,139));
        g.setColor(new Color(229,75,50)); g.fill(new Ellipse2D.Double(25,23,48,12));
        g.fill(new Arc2D.Double(24,73,51,16,180,180,Arc2D.CHORD));
        shape(g,new Ellipse2D.Double(55,9,23,11),new Color(112,175,57));
        g.setColor(new Color(116,68,30)); g.fill(new Ellipse2D.Double(44,50,4,8)); g.fill(new Ellipse2D.Double(54,57,4,8));
    }
    private static void banana(Graphics2D g) {
        g.rotate(-.15,50,50);
        Path2D banana = new Path2D.Double(); banana.moveTo(41,16); banana.curveTo(45,54,32,69,15,76);
        banana.curveTo(18,91,45,88,54,66); banana.curveTo(59,85,78,90,87,80);
        banana.curveTo(66,65,64,43,53,18); banana.closePath();
        gradient(g,banana,new Color(255,225,59),new Color(246,177,22));
        Path2D center = new Path2D.Double(); center.moveTo(47,22); center.curveTo(64,60,53,78,44,88);
        g.setColor(new Color(198,135,27)); g.draw(center);
        shape(g,new RoundRectangle2D.Double(40,9,14,13,3,3),new Color(143,113,45));
        line(g,20,77,17,81,new Color(133,94,29)); line(g,81,80,87,82,new Color(133,94,29));
    }
    private static void battery(Graphics2D g, boolean square) {
        g.rotate(.16,50,50);
        shape(g,new RoundRectangle2D.Double(41,9,18,12,3,3),new Color(163,169,160));
        gradient(g,new RoundRectangle2D.Double(square?25:31,19,square?51:38,72,7,7),new Color(97,105,99),new Color(41,51,47));
        shape(g,new RoundRectangle2D.Double(square?26:32,20,square?49:36,19,5,5),new Color(221,168,68));
        line(g,45,29,55,29,new Color(78,73,48)); line(g,50,24,50,34,new Color(78,73,48));
        line(g,44,79,56,79,new Color(211,215,201));
        g.setColor(new Color(255,255,255,50)); g.fill(new RoundRectangle2D.Double(36,43,5,28,3,3));
    }
    private static void bulb(Graphics2D g) {
        Path2D glass = new Path2D.Double(); glass.moveTo(37,67); glass.lineTo(36,59);
        glass.curveTo(13,33,28,9,49,9); glass.curveTo(75,9,88,34,66,59); glass.lineTo(63,67); glass.closePath();
        gradient(g,glass,new Color(255,255,242),new Color(205,229,224));
        Path2D filament = new Path2D.Double(); filament.moveTo(44,66); filament.lineTo(40,41);
        filament.lineTo(49,45); filament.lineTo(57,39); filament.lineTo(54,67);
        g.setColor(new Color(147,170,155)); g.draw(filament);
        gradient(g,new RoundRectangle2D.Double(36,66,28,19,5,5),new Color(184,195,180),new Color(112,131,124));
        for (int y=70;y<84;y+=5) line(g,37,y,63,y,new Color(83,106,98));
        shape(g,new RoundRectangle2D.Double(42,84,16,7,5,5),new Color(85,103,94));
        g.setColor(new Color(255,255,255,180)); g.setStroke(new BasicStroke(4)); g.draw(new Arc2D.Double(28,18,25,30,95,80,Arc2D.OPEN));
    }

    static void memory(Graphics2D source,int x,int y,int w,int h,int type) {
        Graphics2D g=begin(source,x,y,w,h);
        switch(type) {
            case 0: bin(g,20,10,60,79,0); break;
            case 1: house(g); break;
            case 2: turbines(g); break;
            case 3: bike(g); break;
            case 4: sprout(g); break;
            default: faucet(g);
        }
        g.dispose();
    }
    private static void house(Graphics2D g) {
        shadow(g);
        gradient(g,new Rectangle2D.Double(21,43,59,43),new Color(255,204,116),new Color(223,141,69));
        shape(g,path(12,45,45,15,87,39,79,48,46,25,21,52),new Color(185,81,42));
        shape(g,path(45,20,78,40,61,42,34,25),new Color(54,103,114));
        g.setColor(new Color(147,201,206));
        for(int i=0;i<3;i++) line(g,43+i*9,24+i*4,37+i*9,29+i*5,new Color(161,214,214));
        line(g,39,26,68,42,new Color(161,214,214));
        shape(g,new Rectangle2D.Double(45,60,15,26),new Color(136,84,41));
        shape(g,new Rectangle2D.Double(27,59,12,14),new Color(177,224,220));
        line(g,33,59,33,73,new Color(123,107,66)); line(g,27,66,39,66,new Color(123,107,66));
        shape(g,new Rectangle2D.Double(65,57,9,13),new Color(177,224,220));
        g.setColor(new Color(117,173,58)); g.fill(new Ellipse2D.Double(13,82,27,8)); g.fill(new Ellipse2D.Double(62,82,25,8));
    }
    private static void turbines(Graphics2D g) {
        g.setColor(new Color(147,194,78)); g.fill(new Ellipse2D.Double(8,78,86,15));
        turbine(g,30,43,43); turbine(g,69,30,58);
        g.setColor(new Color(85,151,63)); g.fill(new Ellipse2D.Double(10,83,32,10));
        g.fill(new Ellipse2D.Double(51,85,40,7));
    }
    private static void turbine(Graphics2D g,int x,int y,int height) {
        shape(g,path(x-2,y,x+2,y,x+4,y+height,x-4,y+height),new Color(220,230,220));
        for(int i=0;i<3;i++) {
            Graphics2D a=(Graphics2D)g.create(); a.rotate(i*Math.PI*2/3,x,y);
            shape(a,path(x-2,y-2,x-3,y-27,x,y-33,x+2,y-4),new Color(237,244,232)); a.dispose();
        }
        shape(g,new Ellipse2D.Double(x-4,y-4,8,8),new Color(151,181,171));
    }
    private static void bike(Graphics2D g) {
        g.setStroke(new BasicStroke(3));
        shape(g,new Ellipse2D.Double(6,48,36,36),new Color(249,250,229));
        shape(g,new Ellipse2D.Double(58,48,36,36),new Color(249,250,229));
        g.setStroke(new BasicStroke(1));
        for(int j=0;j<8;j++) {
            double a=j*Math.PI/4; line(g,24,66,24+16*Math.cos(a),66+16*Math.sin(a),new Color(145,164,148));
            line(g,76,66,76+16*Math.cos(a),66+16*Math.sin(a),new Color(145,164,148));
        }
        g.setStroke(new BasicStroke(3.5f));
        g.setColor(new Color(227,82,44)); g.draw(path(24,66,39,41,52,66));
        g.draw(path(39,41,65,41,52,66)); g.draw(new Line2D.Double(65,41,76,66));
        line(g,39,41,35,31,INK); line(g,28,31,42,31,INK);
        line(g,65,41,62,27,INK); line(g,62,27,73,23,INK);
        line(g,52,66,60,70,INK); line(g,59,70,65,70,INK);
    }
    private static void sprout(Graphics2D g) {
        Path2D hand=new Path2D.Double(); hand.moveTo(14,72); hand.lineTo(30,58);
        hand.quadTo(38,53,47,60); hand.lineTo(64,65); hand.quadTo(77,67,71,73);
        hand.lineTo(51,73); hand.quadTo(78,81,84,84); hand.lineTo(84,93);
        hand.lineTo(59,91); hand.quadTo(35,94,14,80); hand.closePath();
        gradient(g,hand,new Color(255,210,155),new Color(235,163,112));
        shape(g,new Ellipse2D.Double(36,51,31,14),new Color(147,102,44));
        g.setStroke(new BasicStroke(3)); line(g,52,57,50,28,new Color(78,133,48));
        shape(g,path(50,36,38,35,28,23,28,16,39,17,48,25),new Color(132,186,57));
        shape(g,path(51,30,54,18,65,11,75,12,71,23,59,31),new Color(110,171,48));
        line(g,34,22,47,32,new Color(77,133,42)); line(g,56,25,68,17,new Color(77,133,42));
    }
    private static void faucet(Graphics2D g) {
        gradient(g,new RoundRectangle2D.Double(18,28,59,20,5,5),new Color(170,186,171),new Color(112,132,119));
        gradient(g,new RoundRectangle2D.Double(60,38,22,21,4,4),new Color(148,169,154),new Color(86,111,99));
        shape(g,new RoundRectangle2D.Double(57,55,28,8,3,3),new Color(79,104,94));
        shape(g,new RoundRectangle2D.Double(17,22,10,32,3,3),new Color(92,124,107));
        line(g,46,29,46,16,INK); g.setStroke(new BasicStroke(5)); line(g,34,16,58,16,new Color(85,115,99));
        shape(g,new Ellipse2D.Double(42,10,9,9),new Color(112,143,119));
        Path2D drop=new Path2D.Double(); drop.moveTo(71,69); drop.curveTo(67,79,61,84,64,90);
        drop.curveTo(69,99,83,93,80,85); drop.closePath();
        gradient(g,drop,new Color(110,219,239),new Color(26,162,206));
        line(g,68,84,67,88,new Color(222,255,252));
    }
}
