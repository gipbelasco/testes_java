# Scraper de Cursos USP

João Pedro Alonso Almeida - NUSP 11832343
Samuel Rubens - NUSP 11912533
Giovanna Belasco - NUSP 

Um web scraper abrangente para extrair informações de cursos e disciplinas do sistema JupiterWeb da Universidade de São Paulo (USP). Esta ferramenta navega através das unidades acadêmicas para coletar dados detalhados sobre cursos e seus currículos.

## Recursos

- **Web Scraping Automatizado**: Navega pela interface JupiterWeb da USP para extrair dados de cursos
- **Extração Abrangente de Dados**: Coleta informações de cursos incluindo:
  - Nome do curso, duração (ideal, mínima, máxima)
  - Disciplinas obrigatórias
  - Disciplinas optativas eletivas
  - Disciplinas optativas livres
  - Detalhes das disciplinas: código, nome, créditos aula, créditos trabalho, carga horária
- **Navegação Robusta**: Usa navegação explícita por URL ao invés de navegação do browser para maior confiabilidade
- **Tratamento de Erros**: Recuperação elegante de erros de parsing e problemas de rede
- **Sistema de Consultas Interativo**: Capacidades de análise e busca pós-scraping
- **Exportação de Dados**: Funcionalidade de exportação CSV para análises posteriores
- **Exportação JSON**: Exportação completa de dados em formato JSON com hierarquia estruturada
- **Limites Configuráveis**: Controle quantas unidades acadêmicas serão processadas

## Arquitetura Técnica

### Componentes

1. **Classe Scraper** (`scraper/Scraper.java`)
   - Lógica principal de web scraping usando Selenium WebDriver
   - Gerencia navegação pela interface JupiterWeb da USP
   - Faz parsing do conteúdo HTML usando JSoup para extração de dados

2. **Modelos de Dados** (`models/`)
   - `Unidade.java`: Representa unidades acadêmicas
   - `Curso.java`: Representa cursos individuais
   - `Disciplina.java`: Representa disciplinas/matérias individuais

3. **Sistema de Consultas** (`app/ConsultasManager.java`)
   - Interface de linha de comando interativa para análise de dados
   - Capacidades de busca e filtro
   - Funcionalidades de estatísticas e exportação

4. **Aplicação Principal** (`app/Main.java`)
   - Ponto de entrada com parsing de argumentos da linha de comando
   - Orquestra o scraping e inicia o sistema de consultas

### Dependências

- **Selenium WebDriver**: Automação de browser
- **WebDriverManager**: Gerenciamento automático do ChromeDriver
- **JSoup**: Parsing HTML e extração de dados
- **Maven**: Gerenciamento de build e dependências

## Instalação

### Pré-requisitos

- Java 11 ou superior
- Maven 3.6+
- Navegador Chrome (para Selenium WebDriver)

### Configuração

1. Clone ou baixe o projeto
2. Navegue até o diretório do projeto
3. Instale as dependências:
   ```bash
   mvn clean compile
   ```

## Uso

### Scraping Básico

```bash
# Processar 2 unidades acadêmicas (padrão)
mvn exec:java

# Processar 5 unidades acadêmicas
mvn exec:java -Dexec.args="5"

# Sintaxe alternativa para limite
mvn exec:java -Dexec.args="--limit=5"

# Exportar para arquivo JSON
mvn exec:java -Dexec.args="1 --json"

# Exportar para arquivo JSON com nome personalizado
mvn exec:java -Dexec.args="2 --json=meus_dados.json"
```

### Modo Interativo

Execute o scraper com capacidades de consulta interativa:

```bash
# Modo interativo real (permite digitação de comandos)
mvn exec:java -Dexec.args="3 --interactive"

# Combinar com exportação JSON
mvn exec:java -Dexec.args="2 --json --interactive"
```

**⚠️ Importante**: Para uso **realmente interativo**, execute apenas o comando acima e aguarde o sistema carregar. Após ver o prompt `Consulta>`, você poderá digitar comandos manualmente.

#### Uso Automatizado (Scripted)
Se você quiser executar comandos automaticamente sem interação manual, use pipes:

```bash
# Execução automatizada de comando específico de estatística
echo -e "statistics\nexit" | mvn exec:java -Dexec.args="--interactive" -q

# Múltiplos comandos automatizados
echo -e "list-units\nstatistics\nexport relatorio\nexit" | mvn exec:java -Dexec.args="1 --interactive" -q
```

### Comandos Interativos

Uma vez no modo interativo, você pode usar estes comandos detalhados:

#### 1. `list-units`
- **Função**: Lista todas as unidades acadêmicas processadas
- **Uso**: `list-units` (sem parâmetros)
- **Saída**: Mostra numeração, nome da unidade e quantidade de cursos
- **Exemplo**:
  ```
  Consulta> list-units
  Unidades Acadêmicas (2 total):
  1. Escola de Comunicações e Artes (16 cursos)
  2. Instituto de Matemática e Estatística (8 cursos)
  ```

#### 2. `list-courses [unidade]`
- **Função**: Lista cursos de uma unidade específica ou de todas as unidades
- **Uso**: 
  - `list-courses` (sem parâmetro) - Lista **todos** os cursos de **todas** as unidades
  - `list-courses "nome da unidade"` - Lista apenas cursos da unidade especificada
- **Busca**: Aceita nomes parciais de unidades (busca case-insensitive)
- **Saída**: Nome do curso e quantidade total de disciplinas
- **Exemplos**:
  ```
  Consulta> list-courses
  Todos os cursos:
  
  Escola de Comunicações e Artes:
    - Bacharelado em Audiovisual (156 disciplinas)
    - Bacharelado em Biblioteconomia (89 disciplinas)
  
  Consulta> list-courses "matemática"
  Cursos em Instituto de Matemática e Estatística:
    - Bacharelado em Ciência da Computação (201 disciplinas)
    - Bacharelado em Estatística (145 disciplinas)
  ```

#### 3. `search-course [nome]`
- **Função**: Busca cursos por nome usando correspondência parcial
- **Uso**: `search-course termo_de_busca` (parâmetro obrigatório)
- **Busca**: Case-insensitive, busca substring no nome do curso
- **Saída**: Lista cursos encontrados com unidade de origem e contagem de disciplinas
- **Exemplos**:
  ```
  Consulta> search-course biotecnologia
  Encontrado(s) 1 curso(s) correspondente(s) a 'biotecnologia':
    - Bacharelado em Biotecnologia (Instituto de Química) - 78 disciplinas
  
  Consulta> search-course computação
  Encontrado(s) 2 curso(s) correspondente(s) a 'computação':
    - Bacharelado em Ciência da Computação (IME) - 201 disciplinas
    - Licenciatura em Computação (EACH) - 134 disciplinas
  ```

#### 4. `search-discipline [nome]`
- **Função**: Busca disciplinas por código ou nome em todos os cursos
- **Uso**: `search-discipline termo_de_busca` (parâmetro obrigatório)
- **Busca**: Case-insensitive, busca em códigos E nomes de disciplinas
- **Saída**: Lista disciplinas únicas encontradas e os cursos onde aparecem
- **Funcionalidade especial**: Agrupa disciplinas duplicadas mostrando todos os cursos onde aparecem
- **Exemplos**:
  ```
  Consulta> search-discipline MAT
  Disciplinas encontradas correspondentes a 'MAT':
    - MAT0111 - Cálculo Diferencial e Integral I
      Encontrada em: Bacharelado em Ciência da Computação, Bacharelado em Estatística
    - MAT2453 - Cálculo Diferencial e Integral para Computação
      Encontrada em: Bacharelado em Sistemas de Informação
  
  Consulta> search-discipline "álgebra linear"
  Disciplinas encontradas correspondentes a 'álgebra linear':
    - MAT0105 - Geometria Analítica e Álgebra Linear
      Encontrada em: Bacharelado em Física, Bacharelado em Química
  ```

#### 5. `course-details [curso]`
- **Função**: Exibe informações completas e detalhadas de um curso
- **Uso**: `course-details "nome do curso"` (parâmetro obrigatório)
- **Busca**: Case-insensitive, busca substring no nome do curso (primeiro encontrado)
- **Saída**: Informações completas incluindo:
  - Nome completo e unidade acadêmica
  - Durações (ideal, mínima, máxima)
  - **Todas** as disciplinas organizadas por categoria com códigos
- **Exemplo**:
  ```
  Consulta> course-details "biotecnologia"
  === Detalhes do Curso ===
  Nome: Bacharelado em Biotecnologia
  Unidade: Instituto de Química
  Duração Ideal: 8 semestres
  Duração Mínima: 8 semestres
  Duração Máxima: 12 semestres
  
  Disciplinas Obrigatórias (61):
    - ACH0021 - Transformações Químicas
    - QFL0111 - Química Geral I
    [... lista completa ...]
  
  Disciplinas Optativas Eletivas (17):
    - QBQ0315 - Bioquímica Experimental
    [... lista completa ...]
  ```

#### 6. `statistics`
- **Função**: Exibe estatísticas abrangentes dos dados processados
- **Uso**: `statistics` (sem parâmetros)
- **Saída**: Métricas completas incluindo:
  - Contagem de unidades, cursos e disciplinas
  - Breakdown por tipo de disciplina (obrigatórias, eletivas, optativas livres)
  - Médias calculadas (cursos por unidade, disciplinas por curso)
- **Exemplo**:
  ```
  Consulta> statistics
  === Estatísticas de Cursos USP ===
  Unidades Acadêmicas: 2
  Total de Cursos: 50
  Total de Disciplinas: 5899
    - Obrigatórias: 4234
    - Eletivas: 1456
    - Optativas Livres: 209
  Média de Cursos por Unidade: 25.0
  Média de Disciplinas por Curso: 117.9
  ```

#### 7. `export [arquivo]`
- **Função**: Exporta todos os dados para arquivo CSV formatado
- **Uso**: 
  - `export` (sem parâmetro) - Usa nome padrão: `exportacao_cursos_usp.csv`
  - `export nome_arquivo` - Usa nome personalizado (extensão .csv adicionada automaticamente)
- **Formato**: CSV com colunas: Unidade, Curso, Tipo_Disciplina, Codigo, Nome, Creditos_Aula, Creditos_Trabalho, Horas
- **Dados**: Inclui **todas** as disciplinas de **todos** os cursos com metadados completos
- **Exemplos**:
  ```
  Consulta> export
  Dados exportados para: exportacao_cursos_usp.csv
  
  Consulta> export meus_dados_usp
  Dados exportados para: meus_dados_usp.csv
  ```

#### 8. `exit`
- **Função**: Encerra o sistema interativo
- **Uso**: `exit` (sem parâmetros)
- **Ação**: Retorna ao terminal com mensagem de despedida

### Exemplos de Uso Interativo

```bash
# Sessão interativa completa demonstrando todas as funcionalidades
Consulta> list-units
Unidades Acadêmicas (2 total):
1. Escola de Comunicações e Artes (16 cursos)  
2. Instituto de Matemática e Estatística (8 cursos)

Consulta> list-courses "matemática"
Cursos em Instituto de Matemática e Estatística:
  - Bacharelado em Ciência da Computação (201 disciplinas)
  - Bacharelado em Estatística (145 disciplinas)

Consulta> search-course biotecnologia
Encontrado(s) 1 curso(s) correspondente(s) a 'biotecnologia':
  - Bacharelado em Biotecnologia (Instituto de Química) - 78 disciplinas

Consulta> course-details "biotecnologia"
=== Detalhes do Curso ===
Nome: Bacharelado em Biotecnologia
Unidade: Instituto de Química
[... informações detalhadas completas ...]

Consulta> search-discipline "cálculo"
Disciplinas encontradas correspondentes a 'cálculo':
  - MAT0111 - Cálculo Diferencial e Integral I
    Encontrada em: Bacharelado em Ciência da Computação, Bacharelado em Estatística
  [... outras disciplinas ...]

Consulta> statistics
=== Estatísticas de Cursos USP ===
Unidades Acadêmicas: 2
Total de Cursos: 50
Total de Disciplinas: 5899
[... estatísticas detalhadas ...]

Consulta> export dados_completos_usp
Dados exportados para: dados_completos_usp.csv

Consulta> exit
Saindo do sistema de consultas. Até logo!
```

### Dicas de Uso do Modo Interativo

- **Busca Flexível**: Todos os comandos de busca (`search-course`, `search-discipline`, `course-details`) usam correspondência parcial case-insensitive
- **Parâmetros Opcionais**: Comandos como `list-courses` e `export` funcionam com ou sem parâmetros
- **Nomes com Espaços**: Use aspas para nomes com espaços: `course-details "ciência da computação"`
- **Disciplinas Duplicadas**: O comando `search-discipline` automaticamente agrupa disciplinas que aparecem em múltiplos cursos
- **Exportação Automática**: Arquivos CSV são criados no diretório atual com formatação padronizada
- **Navegação Intuitiva**: Use `list-units` → `list-courses` → `course-details` para explorar hierarquicamente

## Modelos de Dados

### Unidade (Unidade Acadêmica)
- `nome`: Nome da unidade acadêmica
- `cursos`: Lista de cursos oferecidos por esta unidade

### Curso (Curso)
- `nome`: Nome do curso
- `unidade`: Unidade acadêmica que oferece o curso
- `duracaoIdeal`: Duração ideal (ex: "8 semestres")
- `duracaoMin`: Duração mínima
- `duracaoMax`: Duração máxima
- `obrigatorias`: Lista de disciplinas obrigatórias
- `optativasEletivas`: Lista de disciplinas optativas eletivas
- `optativasLivres`: Lista de disciplinas optativas livres

### Disciplina (Disciplina)
- `codigo`: Código da disciplina (ex: "MAT2453")
- `nome`: Nome da disciplina
- `creditosAula`: Créditos aula
- `creditosTrabalho`: Créditos trabalho
- `cargaHoraria`: Total de horas

## Formato de Exportação JSON

A exportação JSON cria um arquivo estruturado com o seguinte formato:

```json
{
  "timestamp": "Sun Jun 22 20:06:19 GMT-03:00 2025",
  "total_units": 1,
  "units": [
    {
      "name": "Nome da Unidade Acadêmica",
      "total_courses": 16,
      "courses": [
        {
          "name": "Nome do Curso",
          "unit": "Nome da Unidade Acadêmica",
          "duration_ideal": "8 semestres",
          "duration_min": "8 semestres", 
          "duration_max": "12 semestres",
          "mandatory_disciplines": [
            {
              "code": "ACH0021",
              "name": "Nome da Disciplina",
              "credits_class": 2,
              "credits_work": 0,
              "hours": 30
            }
          ],
          "elective_disciplines": [...],
          "free_elective_disciplines": [...],
          "statistics": {
            "total_disciplines": 78,
            "mandatory_count": 61,
            "elective_count": 17,
            "free_elective_count": 0
          }
        }
      ]
    }
  ]
}
```

## Status Atual

### ✅ Recursos Implementados
- Web scraping com navegação explícita por URL
- Extração de dados de cursos e disciplinas
- Tratamento de erros com detecção de sobreposições bloqueantes
- Sistema de consultas interativo com capacidades de busca
- Funcionalidade de exportação CSV
- **Exportação JSON com dados estruturados completos**
- Relatórios estatísticos abrangentes
- Interface de linha de comando com múltiplas opções
- Seleção de elementos baseada em texto para confiabilidade
- Fallback de clique JavaScript para problemas de interferência da UI

### 🔄 Limitações Conhecidas
- Falhas ocasionais de cursos únicos devido a variações na estrutura do site
- Vários cursos de música da Escola de Comunicações de Artes (ECA) sem informações de seus cursos
- Algumas informações de créditos de disciplinas podem não ser totalmente extraídas dependendo da estrutura da página
- Velocidade de scraping limitada pelos tempos de resposta do site e delays necessários
- Datasets grandes podem exigir tratamento de paginação (não implementado atualmente)

### 🚀 Melhorias Futuras
- Opções de persistência em banco de dados
- Interface baseada na web
- Opções avançadas de filtro e ordenação
- Agendamento automatizado para atualizações regulares de dados
- Otimizações de performance para scraping em larga escala

## Notas de Performance

- O scraper inclui delays apropriados para respeitar o site alvo
- Cada curso requer navegação de página separada, então o tempo de scraping escala com o número de cursos
- Performance típica: ~30-60 segundos por unidade acadêmica dependendo da quantidade de cursos
- Uso de memória é proporcional à quantidade de dados processados

## Tratamento de Erros

O scraper inclui tratamento abrangente de erros:

- **Erros de Navegação**: Retry automático com navegação explícita por URL
- **Erros de Parsing**: Pular conteúdo problemático graciosamente com processamento continuado
- **Tratamento de Timeout**: Timeouts do WebDriver de 15 segundos com condições de espera apropriadas
- **Validação de Dados**: Filtragem de linhas inválidas ou de cabeçalho em tabelas de disciplinas

## Contribuindo

Para contribuir com este projeto:

1. Certifique-se de que suas alterações mantêm os padrões de tratamento de erros existentes
2. Teste minuciosamente com várias unidades acadêmicas
3. Atualize a documentação para quaisquer novos recursos
4. Siga as convenções de código Java e inclua comentários apropriados

## Notas Técnicas

### Estratégia de Navegação
O scraper usa navegação explícita por URL (`driver.get(baseUrl)`) ao invés de comandos de navegação do browser para garantir estado confiável da página e evitar se perder no fluxo de navegação do site.

### Abordagem de Parsing
O parsing HTML combina Selenium para carregamento de conteúdo dinâmico e JSoup para extração eficiente de dados, fornecendo tanto capacidade de execução JavaScript quanto suporte poderoso a seletores CSS.

### Integridade dos Dados
O sistema inclui validação para filtrar linhas de cabeçalho, dados vazios e entradas duplicadas enquanto preserva todas as informações válidas de disciplinas.