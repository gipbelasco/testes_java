# Scraper de Cursos USP

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
mvn exec:java -Dexec.args="3 --interactive"

# Combinar com exportação JSON
mvn exec:java -Dexec.args="2 --json --interactive"
```

### Comandos Interativos

Uma vez no modo interativo, você pode usar estes comandos:

- `list-units` - Mostrar todas as unidades acadêmicas processadas
- `list-courses [unidade]` - Mostrar cursos em uma unidade específica ou todos os cursos
- `search-course [nome]` - Buscar cursos por nome
- `search-discipline [nome]` - Buscar disciplinas por nome ou código
- `course-details [curso]` - Mostrar informações detalhadas sobre um curso
- `statistics` - Exibir estatísticas gerais sobre os dados processados
- `export [arquivo]` - Exportar dados para formato CSV
- `exit` - Sair do sistema interativo

### Exemplos

```bash
# Exemplos de sessão interativa
Query> list-units
Query> search-course biotecnologia
Query> course-details "Bacharelado em Biotecnologia"
Query> search-discipline MAT
Query> statistics
Query> export meus_dados_usp
```

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
- Web scraping robusto com navegação explícita por URL
- Extração completa de dados de cursos e disciplinas
- Tratamento de erros pronto para produção com detecção de sobreposições bloqueantes
- Sistema de consultas interativo com capacidades de busca
- Funcionalidade de exportação CSV
- **Exportação JSON com dados estruturados completos**
- Relatórios estatísticos abrangentes
- Interface de linha de comando com múltiplas opções
- Seleção de elementos baseada em texto para confiabilidade
- Fallback de clique JavaScript para problemas de interferência da UI

### 🎯 Métricas de Performance
- **Taxa de Sucesso**: 93,75% (15/16 cursos processados com sucesso)
- **Confiabilidade**: Navegação robusta com recuperação abrangente de erros
- **Qualidade dos Dados**: Extração completa de disciplinas com metadados
- **Escala**: Gerencia com sucesso múltiplas unidades acadêmicas e cursos

### 🔄 Limitações Conhecidas
- Falhas ocasionais de cursos únicos devido a variações na estrutura do site
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