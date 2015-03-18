open ApiAst
open ApiMap


let map_decl v this_name = function

  | Decl_Static (Decl_Namespace _)
  | Decl_Static (Decl_Function _) as decl ->
      decl

  | Decl_Function (Ty_Const (type_name), lname, parameters, error_list) ->
      let this_type = Ty_Const (TypeName.this) in
      let parameters = Param (this_type, this_name) :: parameters in
      Decl_Function (type_name, lname, parameters, error_list)

  | Decl_Function (type_name, lname, parameters, error_list) ->
      let this_type =
        if lname = "get" || lname = "size" then
          Ty_Const (TypeName.this)
        else
          TypeName.this
      in
      let parameters = Param (this_type, this_name) :: parameters in
      Decl_Function (type_name, lname, parameters, error_list)

  | Decl_Class (lname, decls) ->
      let decls = visit_list v.map_decl v lname decls in
      Decl_Class (lname, decls)

  | decl ->
      visit_decl v this_name decl


let v = { default with map_decl }


let transform decls =
  visit_decls v "" decls