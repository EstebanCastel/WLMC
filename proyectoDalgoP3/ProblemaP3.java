// Carlos Casadiego - 202212187
// Esteban Castelblanco Gomez - 202214942
// Felipe Lancheros - 202211004

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ProblemaP3{
    static class GrafoPonderado{
        Set<Integer> vertices;
        Set<Edge> aristas;
        Map<Integer, Integer> pesos;
        Map<Integer, Set<Integer>> listaAdyacencia;

        public GrafoPonderado(Set<Integer> vertices, Set<Edge> aristas, Map<Integer, Integer> pesos) {
            this.vertices = vertices;
            this.aristas = aristas;
            this.pesos = pesos;
            this.listaAdyacencia = crearListaAdyacencia();
        }

        static class Edge{
            int u;
            int v;
            Edge(int u, int v){
                this.u = u;
                this.v = v;
            }
        }

        private Map<Integer, Set<Integer>> crearListaAdyacencia() {
            Map<Integer, Set<Integer>> listaAdyacencia = new HashMap<>();
            int expectedDegree = 5000;
            for (Integer vertice : vertices) {
                listaAdyacencia.put(vertice, new HashSet<>(expectedDegree));
            }
            for (Edge arista : aristas) {
                listaAdyacencia.get(arista.u).add(arista.v);
                listaAdyacencia.get(arista.v).add(arista.u);
            }
            return listaAdyacencia;
        }
        
        public GrafoPonderado subgrafoInducido(Set<Integer> P) {
            Set<Integer> subVertices = new HashSet<>(P);
            subVertices.retainAll(vertices); 
        
            Set<Edge> subAristas = new HashSet<>();
            for (Edge arista : aristas) {
                if (subVertices.contains(arista.u) && subVertices.contains(arista.v)) {
                    subAristas.add(arista);
                }
            }
        
            Map<Integer, Integer> subPesos = new HashMap<>();
            for (Integer vertice : subVertices) {
                subPesos.put(vertice, pesos.get(vertice));
            }
        
            return new GrafoPonderado(subVertices, subAristas, subPesos);
        }

        public int pesoDe(Set<Integer> vertices) {
            int totalPeso = 0;
            for (Integer vertice : vertices) {
                totalPeso += pesos.getOrDefault(vertice, 0);
            }
            return totalPeso;
        }

        public Set<Integer> vecinos(int vertice) {
            return listaAdyacencia.getOrDefault(vertice, new HashSet<>());
        }

        public int grado(int vertice) {
            return vecinos(vertice).size();
        }
        
    }

    public static Triple<Set<Integer>, List<Integer>, GrafoPonderado> inicializar(GrafoPonderado G, int cotaInferior) {
        Set<Integer> U = new HashSet<>(G.vertices);
        Map<Integer, Integer> grados = new HashMap<>();
        for (Integer v : U) {
            grados.put(v, G.grado(v));
        }
    
        Set<Integer> C0 = new HashSet<>();
        List<Integer> O0 = new ArrayList<>();
        while (!U.isEmpty()) {
            
            Integer vi = Collections.min(U, Comparator.comparingInt(grados::get));
           
            if (grados.get(vi) == U.size() - 1) {
                C0 = new HashSet<>(U);
                O0 = new ArrayList<>(U);
                break;
            } else {
                U.remove(vi);
                O0.add(vi);
                for (Integer vecino : G.vecinos(vi)) {
                    if (U.contains(vecino)) {
                        grados.put(vecino, grados.get(vecino) - 1);
                    }
                }
            }
        }
    
        
        if (G.pesoDe(C0) > cotaInferior) {
            cotaInferior = G.pesoDe(C0);
        }
    
        
        Set<Integer> verticesGPrima = new HashSet<>();
        for (Integer v : G.vertices) {
            int weightWithNeighbors = G.pesos.getOrDefault(v, 0);
            for (Integer neighbor : G.vecinos(v)) {
                weightWithNeighbors += G.pesos.getOrDefault(neighbor, 0);
            }
            if (weightWithNeighbors > cotaInferior) {
                verticesGPrima.add(v);
            }
        }
        GrafoPonderado GPrima = G.subgrafoInducido(verticesGPrima);
    
      
        List<Integer> verticesFaltantes = new ArrayList<>(G.vertices);
        verticesFaltantes.removeAll(O0);
        Collections.sort(verticesFaltantes); 
        O0.addAll(verticesFaltantes);
    
        return new Triple<>(C0, O0, GPrima);
    }
    

    static class Triple<X, Y, Z> {
        public final X x;
        public final Y y;
        public final Z z;
        public Triple(X x, Y y, Z z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public static List<Integer> obtenerRamas(GrafoPonderado G, int t, List<Integer> O) {
        Set<Integer> B = new HashSet<>();
        List<Set<Integer>> Pi = new ArrayList<>();
        Set<Integer> V = new HashSet<>(G.vertices); 
    
        while (!V.isEmpty()) {
            
            Integer v = Collections.max(V, Comparator.comparingInt(O::indexOf));
            V.remove(v); 
            boolean encontrado = false;
    
            
            for (Set<Integer> D : Pi) {
                if (Collections.disjoint(G.vecinos(v), D)) { 
                    int sumMaxPesos = sumaMaxPesos(Pi, D, G);
                    int wv = G.pesos.get(v);
    
                    if (sumMaxPesos + wv <= t) {
                        D.add(v);
                        encontrado = true;
                        break;
                    }
                }
            }
    
            if (!encontrado) {
                int sumMaxPesos = sumaMaxPesos(Pi, null, G);
                int wv = G.pesos.get(v);
    
               
                if (sumMaxPesos + wv <= t) {
                    Set<Integer> nuevoSet = new HashSet<>();
                    nuevoSet.add(v);
                    Pi.add(nuevoSet);
                } else { 
                    B.add(v);
                }
            }
        }
    
        
        return B.stream()
                .sorted((v1, v2) -> Integer.compare(O.indexOf(v2), O.indexOf(v1)))
                .collect(Collectors.toList());
    }
    
    private static int sumaMaxPesos(List<Set<Integer>> Pi, Set<Integer> excludeSet, GrafoPonderado G) {
        int sum = 0;
        for (Set<Integer> set : Pi) {
            if (set != excludeSet) {
                int maxWeight = set.stream().mapToInt(G.pesos::get).max().orElse(0);
                sum += maxWeight;
            }
        }
        return sum;
    }

    public static Pair<Integer, List<IndependentSet>> actualizarYDividir(GrafoPonderado G, List<IndependentSet> Pi, int ub, int t) {
        while (!Pi.isEmpty()) {
            
            IndependentSet ISNoMarcado = Pi.stream()
                                            .filter(IS -> !IS.isMarcado())
                                            .findFirst()
                                            .orElse(null);
    
            if (ISNoMarcado != null) {
                int omega = ISNoMarcado.pesoMaximo(G); 
                if (omega > t) {
                    ISNoMarcado.marcar(); 
                    ub -= omega - t;
                } else {
                    
                    List<IndependentSet> nuevosIS = dividirIS(G, ISNoMarcado, omega);
                    Pi.remove(ISNoMarcado);
                    Pi.addAll(nuevosIS);
                    ub = t; 
                }
            }
    
            
            if (ub <= t) {
                Pi.forEach(IndependentSet::marcar);
                break;
            }
        }
    
        
        Pi.forEach(IndependentSet::restaurarVerticesEliminados);
        return new Pair<>(ub, Pi);
    }
    
    public static List<IndependentSet> dividirIS(GrafoPonderado G, IndependentSet IS, int omega) {
        List<IndependentSet> nuevosIS = new ArrayList<>();
        
        List<Integer> sortedVertices = IS.getVertices().stream()
                                         .sorted(Comparator.comparing(G.pesos::get).reversed())
                                         .collect(Collectors.toList());
        
        int sumWeights = 0;
        for (Integer v : sortedVertices) {
            if (sumWeights + G.pesos.get(v) <= omega) {
                sumWeights += G.pesos.get(v);
                IS.getVertices().remove(v);
                nuevosIS.add(new IndependentSet(new HashSet<>(Collections.singletonList(v))));
            }
        }
        if (!IS.getVertices().isEmpty()) {
            nuevosIS.add(0, IS); 
        }
        return nuevosIS;
    }
    

    static class IndependentSet {
        private Set<Integer> vertices;
        private boolean marcado;
        private Set<Integer> verticesEliminados;
        private Integer verticeRepresentativo;
    
        public IndependentSet(Set<Integer> vertices) {
            this.vertices = new HashSet<>(vertices);
            this.marcado = false;
            this.verticesEliminados = new HashSet<>();
            this.verticeRepresentativo = null; 
        }
    
        public void marcar() {
            this.marcado = true;
        }
    
        public boolean isMarcado() {
            return marcado;
        }
    
        public Set<Integer> getVertices() {
            return vertices;
        }
    
        public void eliminarVertices(Set<Integer> verticesAEliminar) {
            verticesEliminados.addAll(verticesAEliminar);
            vertices.removeAll(verticesAEliminar);
        }
    
        public void restaurarVerticesEliminados() {
            vertices.addAll(verticesEliminados);
            verticesEliminados.clear();
        }
    
        public int pesoMaximo(GrafoPonderado G) {
            return vertices.stream().mapToInt(G.pesos::get).max().orElse(0);
        }
    
        public void setVerticeRepresentativo(Integer vertice) {
            this.verticeRepresentativo = vertice;
        }
    
        public Integer getVertice() {
            return verticeRepresentativo;
        }
    }

    static class Pair<X, Y> {
        public final X first;
        public final Y second;

        public Pair(X first, Y second) {
            this.first = first;
            this.second = second;
        }   
    }

    public static Set<Integer> WLMC(GrafoPonderado G, int cotaInferior) {
        
        Triple<Set<Integer>, List<Integer>, GrafoPonderado> inicializacion = inicializar(G, cotaInferior);
        Set<Integer> Cmax = new HashSet<>(inicializacion.x); 
        List<Integer> O0 = inicializacion.y; 
        GrafoPonderado GPrima = inicializacion.z; 
        
        for (int i = O0.size() - 1; i >= 0; i--) {
            int v = O0.get(i);
            Set<Integer> P = new HashSet<>(GPrima.vecinos(v)); 
            P.addAll(O0.subList(i + 1, O0.size())); 
    
            int weight = G.pesos.getOrDefault(v, 0);
            
            if (GPrima.pesoDe(P) + weight > GPrima.pesoDe(Cmax)) {
                
                GrafoPonderado GP = GPrima.subgrafoInducido(P);
                Set<Integer> C = buscarMaxCliquePonderado(GP, new HashSet<>(Cmax), new HashSet<>(), O0);
    
                if (GPrima.pesoDe(C) > GPrima.pesoDe(Cmax)) {
                    Cmax = new HashSet<>(C);
                }
            }
        }
        return Cmax;
    }

    public static Set<Integer> buscarMaxCliquePonderado(GrafoPonderado G, Set<Integer> Cmax, Set<Integer> C, List<Integer> O) {
        if (G.vertices.isEmpty()) {
            return new HashSet<>(C);
        }
    
        int t = G.pesoDe(Cmax) - G.pesoDe(C);
        Set<Integer> B = new HashSet<>(obtenerRamas(G, t, O));
        if (B.isEmpty()) {
            return new HashSet<>(C);
        }
    
        Set<Integer> A = new HashSet<>(G.vertices);
        A.removeAll(B);
    
        List<Integer> B_sorted = B.stream()
                .sorted(Comparator.comparingInt(O::indexOf))
                .collect(Collectors.toList());
    
        for (Integer b : B_sorted) {
            
            Set<Integer> P = new HashSet<>(O.stream().filter(x -> O.indexOf(x) > O.indexOf(b)).collect(Collectors.toSet()));
            P.addAll(A);
    
            
            Set<Integer> C_prime = new HashSet<>(C);
            C_prime.add(b);
    
            
            if (G.pesoDe(C_prime) + G.pesoDe(P) > G.pesoDe(Cmax)) {
                Set<Integer> newC = buscarMaxCliquePonderado(G.subgrafoInducido(P), Cmax, C_prime, O);
                if (G.pesoDe(newC) > G.pesoDe(Cmax)) {
                    Cmax = new HashSet<>(newC);
                }
            }
        }
        return Cmax;
    }
    

    public static void main(String[] args) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            int numCasos = Integer.parseInt(reader.readLine().trim());
            
            for (int caso = 0; caso < numCasos; caso++) {
                String[] aporteStrings = reader.readLine().trim().split(" ");
                int n = aporteStrings.length;
                Set<Integer> vertices = new HashSet<>();
                Set<GrafoPonderado.Edge> aristas = new HashSet<>();
                Map<Integer, Integer> pesos = new HashMap<>();
    
                for (int i = 0; i < n; i++) {
                    try {
                        int aporte = Integer.parseInt(aporteStrings[i]);
                        vertices.add(i + 1);
                        pesos.put(i + 1, aporte);
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing vertex weight: " + aporteStrings[i]);
                        return;
                    }
                }
    
                for (int i = 0; i < n; i++) {
                    String conexions = reader.readLine();
                    if (conexions == null) {
                        System.err.println("Unexpected end of input while reading connections for vertex " + (i + 1));
                        return;
                    }
                    String[] conexionStrings = conexions.trim().split(" ");
                    for (String conexionString : conexionStrings) {
                        try {
                            int conexion = Integer.parseInt(conexionString);
                            int indiceConexion = conexion - 1;
                            if (indiceConexion > i) {
                                aristas.add(new GrafoPonderado.Edge(i + 1, indiceConexion + 1));
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("Error parsing connection index: " + conexionString);
                            return;
                        }
                    }
                }
    
                GrafoPonderado G = new GrafoPonderado(vertices, aristas, pesos);
                Set<Integer> Cmax = WLMC(G, pesos.values().stream().mapToInt(Integer::intValue).sum());
    
                int maxAporte = G.pesoDe(Cmax);
                String indicesPersonas = Cmax.stream().sorted().map(Object::toString).collect(Collectors.joining(" "));
                System.out.println(maxAporte + " " + indicesPersonas);
            }
    
        } catch (IOException e) {
            System.err.println("An error occurred during input/output: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Error parsing input: " + e.getMessage());
        }
    }
    

    
}