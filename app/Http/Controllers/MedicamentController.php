<?php

namespace App\Http\Controllers;

use App\Models\Medicament;
use Illuminate\Http\Request;

class MedicamentController extends Controller
{
    public function index()
    {
        return response()->json(Medicament::all());
    }

    public function show(Medicament $medicament)
    {
        return response()->json($medicament);
    }

    public function store(Request $request)
    {
        $data = $request->validate([
            'nom' => 'required|string|max:255',
            'dosage' => 'required|string|max:50',
            'forme' => 'required|string', // Plus souple pour éviter les erreurs 422
            'fabricant' => 'required|string',
            'date_expiration' => 'required|date',
        ]);

        return response()->json(
            Medicament::create($data),
            201
        );
    }

    public function update(Request $request, Medicament $medicament)
    {
        // On valide aussi pour la mise à jour
        $data = $request->validate([
            'nom' => 'string|max:255',
            'dosage' => 'string|max:50',
            'forme' => 'string',
            'fabricant' => 'string',
            'date_expiration' => 'date',
        ]);

        $medicament->update($data);

        return response()->json($medicament);
    }

    public function destroy(Medicament $medicament)
    {
        $medicament->delete();

        return response()->json(["message" => "Supprimé avec succès"], 200); 
        // 204 est correct mais 200 avec un message est plus facile pour débugger sur Android
    }
}