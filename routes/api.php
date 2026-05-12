<?php

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;
use App\Http\Controllers\MedicamentController;

// Cette ligne gere automatiquement les routes GET, POST, PUT et DELETE
Route::apiResource('medicaments', MedicamentController::class);

// Vous pouvez supprimer ou commenter ceci si vous n'utilisez pas d'authentification
// Route::get('/user', function (Request $request) {
//     return $request->user();
// })->middleware('auth:sanctum');