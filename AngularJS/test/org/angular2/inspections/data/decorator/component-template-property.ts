// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Directive} from "@angular/core";

@Component(<error descr="NoProp doesnt have template">{}</error>)
export class NoProp {

}

@Component({
    templateUrl: ""
})
export class Prop1 {

}


@Component({
    template: ""
})
export class Prop2 {

}


@Component({
    <error descr="'template' and 'templateUrl' can't be used together">template</error>: "foo",
    <error descr="'template' and 'templateUrl' can't be used together">templateUrl</error>: "bar"
})
export class PropBoth {

}

@Directive({

})
export class Dir {

}