; AutoReplace.AHK
; Corrige automatiquement les fautes de frappe les plus courantes en français et en anglais, avec un bonus pour les programmeurs

; Français
:*:ajotue::ajoute
:*:alros::alors
::attendnat::attendant
:C:AVec::Avec
:*:ba^ti::bâti
:*:ce'st::c'est
::concernnat::concernant
:C*:çA::ça
::4a::ça
:C:Ca::Ça
::dnas::dans
:C:DAns::Dans
:C*:ELle::Elle
:*:elel::elle
:*:élémetn::élément
::e^tre::être
:*:fa4on::façon
:C:IL::Il
:C:ILs::Ils
::ja'i::j'ai
:C:LA::La
:C:LE::Le
:C:LEs::Les
:*:lesquelels ::lesquelles
:*:maintenatn::maintenant
:*:maintennat::maintenant
:*:ne'st::n'est
:*:nouvelel::nouvelle
:C*:NOn::Non
:C:oÛ::où
:C*:OUi::Oui
:*:aps::pas
:*:réposne::réponse
:*?:séleciton::sélection
:*:su^r::sûr
:*:télécahrg::télécharg
:*:totu::tout
:*:utilsi::utilis

;Anglais
::lsit::list
::lsits::lists
::optiosn::options
::taht::that
::tihs::this
:*:langauge::language

;Spécial programmeurs
:*?:-<::->
::$_PSOT::$_POST
:C*?:ARray::Array
:C*?:ATtrib::Attrib
:*:cosnt::const
::clsoe::close
::defien::define
::fllor::floor
::flaot::float
::funciton::function
:C*?:GEt::Get
::improt::import
:C*?:INput::Input
::isnert::insert
::inserto::insert into
::itn::int
:*:localshot::localhost
:*:localhsot::localhost
:C*?:MEssage::Message
:*:pasre::parse
:*:aprse::parse
:*?:prinltn::println
::publci::public
:C*?:REad::Read
::rquire::require
::retrn::return
:C*?:SEnd::Send
:C*?:SEt::Set
:*?:Strnig::String
:*?:Strign::String
:C*?:STring::String
:C*?:STream::Stream
:C*?:TAg::Tag
::tempalte::template
::udpate::update
:C*?:WRite::Write

;Raccourcis spéciaux
;
; Ctrl+Alt+6 fait normalement le caractère ¬ qui ne sert absolument à rien sauf provoquer des erreurs d'inattention idiotes
^!6::Send |



