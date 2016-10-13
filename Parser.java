//written by Andre Betz 2007
//http://www.andrebetz.de
      
public class Parser {
	public static class ParseTree {
		private ParseTree m_Next = null;
		private ParseTree m_Act = null;
		private ParseTree m_Start = null;
		private String m_Symbol = null;

		ParseTree(){
		}
		ParseTree(String Symbol){
			m_Symbol = Symbol;
		}
		public void set_Symbol(String Symbol){
			m_Symbol = Symbol;
		}
		public String get_Symbol(){
			return m_Symbol;
		}
		public ParseTree getM_Next() {
			return m_Next;
		}
		public ParseTree getM_Start() {
			return m_Start;
		}
		public void AddList(ParseTree pt) {
			if(pt!=null){
				if(m_Start==null){
					m_Start = pt;
					m_Act = pt;
				}else{
					m_Act = m_Act.setM_Next(pt);
				}
			}
		}
		public ParseTree setM_Next(ParseTree next) {
			m_Next = next;
			return next;
		}
	}
	private Scanner m_scn = null;
	private ParseTree m_pt = null;
	private Scanner.TokenSym m_actSym = null;
	private String Errors = "";
	private boolean bParseError = false;
	
	Parser(Scanner scn){
		m_scn = scn;
	}
	public boolean Parse(){
		m_actSym = m_scn.GetSym();
		m_pt = program();
		m_scn.Reset();
		return bParseError;
	}
	public Scanner.TokenSym GetActSymobl(){
		return m_actSym;
	}
	public ParseTree getAST(){
		return m_pt;
	}
	public String getErrorStr(){
		return Errors;
	}
	private String Next(boolean err){
		String Symbol = m_actSym.getElement();
		m_actSym = m_scn.GetSym();
		if(err){
			bParseError = true;
			Errors += "Line" + m_actSym.getLine() + ": Wrong Symbol: " + Symbol + "\n";
		}
		return Symbol;
	}
	private boolean accept(String Symbol){
		boolean noerr = true;
		if(m_actSym!=null && m_actSym.getElement().equalsIgnoreCase(Symbol)){
			
		}else{
			bParseError = true;
			Errors += "Line" + m_actSym.getLine() + ": No Symbol: " + Symbol + "\n";
			noerr = false;
		}
		m_actSym = m_scn.GetSym();
		return noerr;
	}
	private boolean scan(String Symbol){
		boolean noerr = true;
		if(m_actSym!=null && m_actSym.getElement().length()>0 && m_actSym.getElement().equalsIgnoreCase(Symbol)){
			m_actSym = m_scn.GetSym();		
		}else{
			noerr = false;
		}
		return noerr;
	}
	private boolean IsElemSym(String Symbol) {
		boolean noerr = true;
		if(m_actSym!=null && m_actSym.getElement().length()>0 && m_actSym.getElement().equalsIgnoreCase(Symbol)){
		}else{
			noerr = false;
		}
		return noerr;
	}
	private boolean IsElemType(int type) {
		boolean noerr = true;
		if(m_actSym!=null && m_actSym.getElement().length()>0 && m_actSym.getType()==type){
		}else{
			noerr = false;
		}
		return noerr;
	}
	private boolean IsRelOp(){
		if(IsElemType(Scanner.TokenSym.TOKEN) && 
				  (IsElemSym("=") ||
				   IsElemSym("<>") ||
				   IsElemSym("<") ||
				   IsElemSym("<=")||
				   IsElemSym(">") ||
				   IsElemSym(">="))) 
		{
			return true;
		}
		return false;
	}
	private boolean IsAddOp(){
		if(IsElemType(Scanner.TokenSym.TOKEN) && 
				  (IsElemSym("+") ||
				   IsElemSym("-") ||
				   IsElemSym("OR"))) 
		{
			return true;
		}
		return false;
	}
	private boolean IsMulOp(){
		if(IsElemType(Scanner.TokenSym.TOKEN) && 
				  (IsElemSym("*") ||
				   IsElemSym("/") ||
				   IsElemSym("DIV") ||
				   IsElemSym("AND"))) 
		{
			return true;
		}
		return false;
	}
	private String acceptIdent(){
		String ident = "";
		if(IsElemType(Scanner.TokenSym.IDENTIFIER)){
			ident = m_actSym.getElement();
		}else{
			bParseError = true;
			Errors += "Line" + m_actSym.getLine() + ": No Identifier found\n";
		}
		m_actSym = m_scn.GetSym();
		return ident;
	}
	private String acceptToken(){
		String ident = "";
		if(IsElemType(Scanner.TokenSym.TOKEN)){
			ident = m_actSym.getElement();
		}else{
			bParseError = true;
			Errors += "Line" + m_actSym.getLine() + ": No Token found\n";
		}
		m_actSym = m_scn.GetSym();
		return ident;
	}
	private String acceptType(int type){
		String ident = "";
		if(IsElemType(type)){
			ident = m_actSym.getElement();
		}else{
			bParseError = true;
			Errors += "Line" + m_actSym.getLine() + ": No type " +type+" found\n";
		}
		m_actSym = m_scn.GetSym();
		return ident;
	}
	private String acceptInt(){
		String ident = "";
		if(IsElemType(Scanner.TokenSym.CONSTANTINT)){
			ident = m_actSym.getElement();
		}else{
			bParseError = true;
			Errors += "Line" + m_actSym.getLine() + ": No Integer found\n";
		}
		m_actSym = m_scn.GetSym();
		return ident;
	}
	private ParseTree program() {
		accept("PROGRAM"); 
		String symbol = acceptIdent (); 
		accept(";"); 
		ParseTree pt = block();
		pt.set_Symbol(symbol);  
		accept("."); 
		return pt; 
	}
	private ParseTree block(){
		ParseTree pt = new ParseTree("{");  
		if(scan("VAR")){
			do { 
				variable_decl(pt);
				accept(";"); 
			} while (IsElemType(Scanner.TokenSym.IDENTIFIER)); 
		}
		while (IsElemSym("PROCEDURE")&&IsElemType(Scanner.TokenSym.TOKEN)) {
			pt.AddList(procedure_decl ());
			accept(";"); 
		}
		pt.AddList(compound_statement ());
		return pt;   
	}
	private void variable_decl (ParseTree pt) {
		ParseTree n = new ParseTree(acceptIdent());
		ParseTree tp = n;
		while(scan(",")){
			tp = tp.setM_Next(new ParseTree(acceptIdent()));
		}
		accept(":");
		ParseTree typ = type(); 
		for(tp=n;tp!=null;tp=tp.getM_Next()) {
			ParseTree nt = new ParseTree("VAR");
			nt.AddList(new ParseTree(tp.get_Symbol()));
			nt.AddList(typ);
			pt.AddList(nt);
		}
	}
	private ParseTree type() { 
		ParseTree typ = null; 
		if (IsElemSym("INTEGER")&&IsElemType(Scanner.TokenSym.TOKEN)) {  
			typ = simple_type (); 
		} else {
			accept("ARRAY");
			typ = new ParseTree("ARRAY");  
			accept("["); 
			typ.AddList(index_range());
			accept("]"); 
			accept("OF"); 
			typ.AddList(simple_type()); 
		}
		return typ; 
	}
	private ParseTree simple_type () {
		ParseTree pt = null;
		if(scan("INTEGER")){
			pt = new ParseTree("INTEGER");
		}else if(scan("CHAR")){
			pt = new ParseTree("CHAR");		
		}
		return pt;
	} 
	private ParseTree index_range () { 
		ParseTree range = new ParseTree(".."); 
		range.AddList(new ParseTree(acceptInt())); 
		accept(".."); 
		range.AddList(new ParseTree(acceptInt())); 
		return range; 
	}
	private ParseTree procedure_decl () { 
		accept("PROCEDURE");
		ParseTree proc = new ParseTree("PROCEDURE");
		proc.AddList(new ParseTree(acceptIdent ()));
		accept("(");
		accept(")");
		proc.AddList(block ());
		return proc; 
	}
	private ParseTree compound_statement () { 
		accept ("BEGIN"); 
		ParseTree seq = new ParseTree("{"); 
		seq.AddList(statement ());
//		accept(";"); 
		while(!IsElemSym("END")) {
			seq.AddList(statement ());
//			accept(";"); 
			if(m_actSym==null){ 
				break;
			} 
		}
		accept("END"); 
		return seq; 
	}
	private ParseTree statement () {
		ParseTree stat = null; 
		if(IsElemType(Scanner.TokenSym.IDENTIFIER)){
			String symbol = acceptIdent();
			if(scan("(")){
				stat = new ParseTree("PROC_CALL");
				stat.AddList(new ParseTree(symbol));
				accept(")");
			}else{
				stat = assignment (symbol);
			}
			accept(";");
		}else if(IsElemType(Scanner.TokenSym.TOKEN)){
			if(IsElemSym("BEGIN")) {
				stat = compound_statement ();
			}else if(IsElemSym("IF")) {
			      stat = if_statement ();
			}else if(IsElemSym("WHILE")) {
			      stat = while_statement ();
			}
		}else{
			stat = new ParseTree(Next(true));
		}
		return stat;
	}
	private ParseTree if_statement (){ 
		accept("IF"); 
		ParseTree stat = new ParseTree("IF"); 
		stat.AddList(expression()); 
		accept("THEN"); 
   		stat.AddList(statement ());
		if(scan("ELSE")){
			stat.AddList(statement ());
      	}
		return stat; 
	}
	private ParseTree while_statement () { 
		accept("WHILE"); 
   		ParseTree stat = new ParseTree("WHILE"); 
		stat.AddList(expression ()); 
		accept("DO"); 
		stat.AddList(statement ());
		return stat;  
	}
	private ParseTree assignment (String name) { 
		ParseTree assign = new ParseTree(":=");
		assign.AddList(variable_assign(name));
		accept (":=");
		assign.AddList(expression());
		return assign;  
	}
	private ParseTree variable_assign (String name) { 
		ParseTree expr = null; 
		if(scan("[")){
			expr = new ParseTree("["); 
			expr.AddList(new ParseTree(name)); 
			expr.AddList(expression ()); 
			accept ("]"); 
		}else{ 
      		expr = new ParseTree(name);
		} 
      	return expr; 
	}
	private ParseTree variable () { 
		String name = acceptIdent ();
		ParseTree expr = null; 
		if(scan("[")){
			expr = new ParseTree("["); 
			expr.AddList(new ParseTree(name)); 
			expr.AddList(expression ()); 
			accept ("]"); 
		}else{ 
      		expr = new ParseTree(name);
		} 
      	return expr; 
	}
	private ParseTree expression () { 
		ParseTree expr = simple_expression (); 
		if(IsRelOp()) {
			ParseTree rel = new ParseTree(acceptToken()); 
			rel.AddList(expr); 
			rel.AddList(simple_expression ());
			return rel;  
		}
   		return expr; 
	}
	private ParseTree simple_expression () { 
		boolean Add = false;
		String AddSym = "";
		if(IsElemSym("-") || IsElemSym("+")) {
      		Add = true;
      		AddSym = acceptToken();
      	}
		ParseTree expr = term();
   		if(Add){
   			ParseTree e = new ParseTree(AddSym);
      		e.AddList(expr);
      		expr = e; 
   		}
   		while(IsAddOp()) {
   			ParseTree e = new ParseTree(acceptToken()); 
      		e.AddList(expr); 
      		e.AddList(term()); 
      		expr = e; 
   		} 
   		return expr; 
	} 
	private ParseTree term () { 
		ParseTree expr = factor ();
		while(IsMulOp()) {
			ParseTree e = new ParseTree (acceptToken()); 
      		e.AddList(expr); 
      		e.AddList(factor());
      		expr = e; 
   		}
   		return expr; 
	}
	private ParseTree factor () { 
		ParseTree fac = null;
		if(IsElemType(Scanner.TokenSym.IDENTIFIER)){
			ParseTree vp = variable();
			if(scan("(")){
				fac = new ParseTree("PROC_CALL");
				fac.AddList(vp);
				accept("(");
				accept(")");
			}else{
				fac = vp;
			}
		}else if(IsElemType(Scanner.TokenSym.CONSTANTINT)){
			fac = new ParseTree("CONSTANTINT");
			fac.AddList(new ParseTree(acceptInt()));			
		}else if(IsElemType(Scanner.TokenSym.CONSTANTLIT)){
			fac = new ParseTree("CONSTANTLIT");
			fac.AddList(new ParseTree(acceptType(Scanner.TokenSym.CONSTANTLIT)));			
		}else if(scan("(")){
   			ParseTree expr = expression ();
   			accept(")");
   			fac = expr; 			
		}else if(scan("NOT")){
   			ParseTree expr = new ParseTree("NOT"); 
   			expr.AddList(factor ()); 
   			fac = expr;			
		}else{
			fac = new ParseTree(Next(true));
		}
		return fac;
	}
}
