# Parte B: Questões Dissertativas (Reflexão)

Respostas detalhadas e fundamentadas no código e conceitos do projeto **AppRoomProduto**.

---

### 1. O que é persistência de dados? O que aconteceria com os produtos cadastrados se, em vez do Room, eles fossem guardados apenas em uma MutableList dentro da MainActivity?
**Resposta:**
Persistência de dados refere-se ao armazenamento de informações de forma duradoura em um meio não volátil (como o armazenamento interno do smartphone), permitindo que os dados permaneçam salvos mesmo após fechar o aplicativo ou reiniciar o aparelho.

Se utilizássemos apenas uma `MutableList` na `MainActivity`, os dados existiriam exclusivamente na memória RAM enquanto a Activity estivesse ativa. Ao fechar o app, girar a tela ou o sistema operacional decidir matar o processo do aplicativo por falta de memória, a lista seria completamente apagada. Com o Room (persistência local baseada em SQLite), os produtos são gravados em arquivo no dispositivo, garantindo que estejam disponíveis sempre que o usuário abrir o app novamente.

---

### 2. Qual é a diferença entre usar o SQLite diretamente (com SQLiteOpenHelper e comandos SQL manuais) e usar o Room? Cite pelo menos duas vantagens do Room.
**Resposta:**
O uso direto do SQLite exige escrever e gerenciar consultas SQL brutas como strings, criar cursores (`Cursor`) manuais para traduzir linhas de banco em objetos de domínio e escrever grande quantidade de código repetitivo (*boilerplate*) em subclasses de `SQLiteOpenHelper`. O Room atua como uma biblioteca de abstração robusta sobre o SQLite que automatiza grande parte desse processo.

**Duas principais vantagens do Room:**
1. **Verificação de consultas em tempo de compilação:** O compilador valida a sintaxe das queries SQL (como `@Query("SELECT * FROM product")`) e das entidades em tempo de compilação, impedindo erros de digitação ou colunas inexistentes que só seriam percebidos durante a execução no SQLite puro.
2. **Mapeamento automático objeto-relacional (ORM):** Converte tabelas e resultados SQL diretamente em objetos Kotlin (`List<Product>`) por meio de anotações (`@Entity`, `@Dao`), eliminando a necessidade de conversões manuais de `Cursor`.

---

### 3. Explique o papel de cada uma das três peças do Room: Entity, DAO e Database. Como elas se relacionam no seu projeto?
**Resposta:**
- **Entity (Entidade):** Modela e define a estrutura da tabela no banco de dados. No projeto, a data class `Product` anotada com `@Entity` representa a tabela `product` e suas colunas (`id`, `name`, `price`, `description`, `quantity`).
- **DAO (Data Access Object):** Contém os métodos que definem as operações de acesso ao banco (consultas, inserções, atualizações e exclusões). No projeto, a interface `ProductDao` centraliza operações como `getAll()`, `insertAll()`, `buscarPorNome()`, `updateProduct()` e `deleteAll()`.
- **Database (Banco de Dados):** Classe abstrata que herda de `RoomDatabase`, funcionando como o ponto central de conexão com o banco SQLite. No projeto, a classe `AppDatabase` agrupa as entidades e fornece a instância do `ProductDao`.

**Relação:** A `MainActivity` inicializa o `AppDatabase` via `Room.databaseBuilder()`. Através do banco, obtém-se o `ProductDao`, que executa comandos assíncronos na tabela mapeada pela `Entity` (`Product`).

---

### 4. Por que o Android não permite acessar o banco de dados na thread principal (UI thread)? O que é um ANR (Application Not Responding)? Como suspend e lifecycleScope.launch resolvem esse problema?
**Resposta:**
O acesso a operações de I/O (como banco de dados e rede) é bloqueado na thread principal porque esta é responsável por processar eventos de toque e renderizar a interface gráfica (60+ quadros por segundo). Se uma consulta demorada congelar essa thread, o sistema exibe um alerta de **ANR (Application Not Responding)**, congelando e forçando o fechamento do app após 5 segundos sem resposta.

Os modificadores `suspend` nas funções do DAO indicam operações assíncronas que podem ser pausadas e retomadas, enquanto o `lifecycleScope.launch` abre uma corrotina vinculada ao ciclo de vida da Activity. Isso permite executar as consultas ao banco em uma thread de segundo plano (IO), mantendo a thread principal livre e fluida, e retornando o resultado com segurança.

---

### 5. O que faz o fallbackToDestructiveMigration()? Por que ele é perigoso em um app publicado na Play Store? O que seria uma migração (Migration) de banco de dados?
**Resposta:**
O `fallbackToDestructiveMigration()` orienta o Room a destruir todas as tabelas e dados existentes e recriar o banco do zero caso ocorra uma alteração na versão do esquema (`version`) sem uma rota de migração definida.

É extremamente perigoso em apps em produção na Play Store porque, ao atualizar o aplicativo, **todos os dados salvos pelos usuários serão permanentemente apagados**, gerando perda de informações críticas. Uma **Migration** é um script explícito que instrui o Room a executar comandos SQL específicos (como `ALTER TABLE ... ADD COLUMN ...`) para atualizar o banco da versão antiga para a nova preservando os dados dos usuários.

---

### 6. Qual é a vantagem do ViewBinding em relação ao findViewById? Que tipo de erro ele ajuda a evitar?
**Resposta:**
O `ViewBinding` gera automaticamente uma classe de vinculação para cada arquivo de layout inflado, permitindo acessar diretamente os componentes visuais por meio de referências fortemente tipadas. Em contrapartida, o `findViewById` busca views por ID em tempo de execução percorrendo toda a árvore de componentes.

**Erros que ajuda a evitar:**
1. **NullPointerException:** Ocorre quando um ID é digitado errado ou não existe em determinada orientação de tela. O ViewBinding garante segurança de nulos e tipos em tempo de compilação.
2. **Incompatibilidade de tipos (Cast exception):** Evita tentar atribuir uma view incorreta a uma variável (ex: tentar associar um botão a um EditText).

---

### 7. No tutorial, o banco é criado dentro da MainActivity. Por que em apps maiores é recomendado criar o banco como Singleton (uma única instância para o app inteiro)? O que poderia acontecer se várias telas criassem cada um o seu banco?
**Resposta:**
Criar o banco como **Singleton** assegura que exista apenas uma única instância da conexão com o banco aberta durante toda a execução do aplicativo, otimizando o uso de memória e controlando o acesso concorrente ao arquivo SQLite.

Se várias telas criassem instâncias independentes do banco simultaneamente, haveria um alto consumo de memória, sobrecarga no sistema de arquivos e riscos severos de conflitos de concorrência e corrupção de dados ao tentar gravar no mesmo arquivo SQLite por múltiplos acessos não sincronizados.

---

### 8. Reflexão pessoal: quais foram as principais dificuldades que você teve neste projeto e como as resolveu? Cite um app real (de comércio, estoque, saúde, escola etc.) em que a persistência local com Room seria útil e explique como você o aplicaria.
**Resposta:**
- **Dificuldades e soluções:** Durante o desenvolvimento, um desafio comum em ambientes modernos do Android (com AGP e Kotlin recentes) envolve a compatibilidade entre o plugin `kapt` e versões recentes de metadados do Kotlin. Resolvemos isso ajustando as dependências do Room para a versão `2.8.5`, que conta com total suporte e estabilidade de compilação.
- **Exemplo de app real:** Um aplicativo móvel de **Controle de Estoque e Vendas para Microempreendedores (Mercearias e Lojas de Bairro)**.
- **Aplicação prática:** O Room seria essencial para manter o catálogo de produtos (nome, preço de custo/venda, quantidade em estoque) e o registro de vendas salvos localmente no dispositivo. Como pequenos comércios frequentemente enfrentam instabilidade ou falta de internet, o comerciante poderia realizar vendas, dar baixa no estoque e consultar relatórios offline em tempo real, com a garantia de que os dados estão seguros na persistência local do Room.
